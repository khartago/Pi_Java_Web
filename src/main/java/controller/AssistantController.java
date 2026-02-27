package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Materiel;
import model.MaterielDAO;
import model.Produit;
import model.ProduitDAO;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Assistant "IA" local :
 * - Recherche par nom (SQL LIKE + tri)
 * - Recherche par image (match hash / nom de fichier / chemin)
 * Le résultat affiche une fiche produit + liste des matériels associés.
 */
public class AssistantController {

    @FXML private TextField nomProduitField;
    @FXML private Label selectedImageLabel;
    @FXML private ImageView selectedImagePreview;

    @FXML private TableView<Produit> resultsTable;
    @FXML private TableColumn<Produit, Integer> resIdCol;
    @FXML private TableColumn<Produit, String> resNomCol;
    @FXML private TableColumn<Produit, String> resStockCol;
    @FXML private TableColumn<Produit, String> resExpCol;

    @FXML private Label detailTitleLabel;
    @FXML private ImageView detailImageView;
    @FXML private Label detailIdLabel;
    @FXML private Label detailNomLabel;
    @FXML private Label detailStockLabel;
    @FXML private Label detailUniteLabel;
    @FXML private Label detailExpLabel;
    @FXML private Label detailImagePathLabel;
    @FXML private Label matchInfoLabel;

    @FXML private TableView<Materiel> materielTable;
    @FXML private TableColumn<Materiel, Integer> matIdCol;
    @FXML private TableColumn<Materiel, String> matNomCol;
    @FXML private TableColumn<Materiel, String> matEtatCol;
    @FXML private TableColumn<Materiel, String> matDateCol;
    @FXML private TableColumn<Materiel, Double> matCoutCol;

    private final ProduitDAO produitDAO = new ProduitDAO();
    private final MaterielDAO materielDAO = new MaterielDAO();

    private File selectedImageFile;
    private final Map<Integer, String> matchReasonByProduitId = new HashMap<>();
    private final Map<String, String> fileHashCache = new HashMap<>(); // path -> sha256

    @FXML
    private void initialize() {
        // Results table
        resIdCol.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
        resNomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        resStockCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getQuantite() + " " + safe(cell.getValue().getUnite()))
        );
        resExpCol.setCellValueFactory(cell -> {
            if (cell.getValue().getDateExpiration() == null) return new SimpleStringProperty("-");
            return new SimpleStringProperty(cell.getValue().getDateExpiration().format(DateTimeFormatter.ISO_DATE));
        });

        resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) showProduitDetails(newV);
        });

        // Materiel table
        matIdCol.setCellValueFactory(new PropertyValueFactory<>("idMateriel"));
        matNomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        matEtatCol.setCellValueFactory(new PropertyValueFactory<>("etat"));
        matCoutCol.setCellValueFactory(new PropertyValueFactory<>("cout"));
        matDateCol.setCellValueFactory(cell -> {
            if (cell.getValue().getDateAchat() == null) return new SimpleStringProperty("-");
            return new SimpleStringProperty(cell.getValue().getDateAchat().format(DateTimeFormatter.ISO_DATE));
        });

        // UI defaults
        selectedImageLabel.setText("Aucune image sélectionnée");
        selectedImagePreview.setImage(null);
        clearDetails();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/produit_list.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) resultsTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion des Produits et Matériels");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation", "Impossible de retourner à la liste des produits.");
        }
    }

    @FXML
    private void handleSearchByName() {
        String q = nomProduitField.getText() == null ? "" : nomProduitField.getText().trim();
        if (q.isEmpty()) {
            showWarning("Recherche", "Veuillez saisir un nom de produit.");
            return;
        }

        matchReasonByProduitId.clear();
        List<Produit> results = produitDAO.searchByNameLike(q, 50);
        if (results.isEmpty()) {
            clearDetails();
            resultsTable.setItems(FXCollections.observableArrayList());
            showWarning("Aucun résultat", "Aucun produit ne correspond à: " + q);
            return;
        }

        for (Produit p : results) {
            matchReasonByProduitId.put(p.getIdProduit(), "Match par nom (LIKE) : \"" + q + "\"");
        }

        setResults(results);
        resultsTable.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image produit");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        File file = fc.showOpenDialog(resultsTable.getScene().getWindow());
        if (file == null) return;

        selectedImageFile = file;
        selectedImageLabel.setText(file.getAbsolutePath());
        try {
            selectedImagePreview.setImage(new Image(file.toURI().toString(), true));
        } catch (Exception e) {
            selectedImagePreview.setImage(null);
        }
    }

    @FXML
    private void handleAnalyzeImage() {
        if (selectedImageFile == null) {
            showWarning("Image", "Veuillez choisir une image d'abord.");
            return;
        }

        List<Produit> all = produitDAO.getAll();
        if (all.isEmpty()) {
            showWarning("Base vide", "Aucun produit trouvé dans la base.");
            return;
        }

        String targetPath = normalizePath(selectedImageFile.getAbsolutePath());
        String targetName = normalizeText(stripExtension(selectedImageFile.getName()));
        String targetHash = safeSha256(selectedImageFile.getAbsolutePath());

        List<Scored> scored = new ArrayList<>();
        matchReasonByProduitId.clear();

        for (Produit p : all) {
            String imgPath = p.getImagePath();
            if (imgPath == null || imgPath.isBlank()) continue;

            int score = 0;
            List<String> reasons = new ArrayList<>();

            // 1) Même chemin (souvent le cas si l’utilisateur choisit exactement la même image)
            String normImgPath = normalizePath(imgPath);
            if (!normImgPath.isEmpty() && normImgPath.equals(targetPath)) {
                score += 100;
                reasons.add("Chemin image identique");
            }

            // 2) Même nom de fichier
            String fileName = normalizeText(stripExtension(new File(imgPath).getName()));
            if (!fileName.isEmpty() && !targetName.isEmpty() && fileName.equals(targetName)) {
                score += 60;
                reasons.add("Nom de fichier identique");
            }

            // 3) Hash identique (si les 2 chemins existent localement)
            String pHash = safeSha256(imgPath);
            if (targetHash != null && pHash != null && targetHash.equals(pHash)) {
                score += 120;
                reasons.add("Image identique (SHA-256)");
            }

            // 4) Similarité (fallback léger)
            int sim = simpleSimilarityScore(targetName, normalizeText(p.getNom()));
            if (sim > 0) {
                score += sim;
                reasons.add("Similarité nom (fallback)");
            }

            if (score > 0) {
                scored.add(new Scored(p, score, String.join(" • ", reasons)));
            }
        }

        if (scored.isEmpty()) {
            clearDetails();
            resultsTable.setItems(FXCollections.observableArrayList());
            showWarning("Aucun match", "Impossible d’identifier le produit depuis l’image. Essayez avec le nom du produit.");
            return;
        }

        scored.sort((a, b) -> Integer.compare(b.score, a.score));
        List<Produit> results = scored.stream().map(s -> s.produit).collect(Collectors.toList());
        for (Scored s : scored) {
            matchReasonByProduitId.put(s.produit.getIdProduit(), "Match image: " + s.reason + " (score=" + s.score + ")");
        }

        setResults(results);
        resultsTable.getSelectionModel().selectFirst();
    }

    private void setResults(List<Produit> results) {
        ObservableList<Produit> items = FXCollections.observableArrayList(results);
        resultsTable.setItems(items);
    }

    private void showProduitDetails(Produit p) {
        detailTitleLabel.setText("Fiche produit");
        detailIdLabel.setText(String.valueOf(p.getIdProduit()));
        detailNomLabel.setText(safe(p.getNom()));
        detailStockLabel.setText(String.valueOf(p.getQuantite()));
        detailUniteLabel.setText(safe(p.getUnite()));
        detailExpLabel.setText(p.getDateExpiration() == null ? "-" : p.getDateExpiration().format(DateTimeFormatter.ISO_DATE));
        detailImagePathLabel.setText(p.getImagePath() == null || p.getImagePath().isBlank() ? "-" : p.getImagePath());

        matchInfoLabel.setText(matchReasonByProduitId.getOrDefault(p.getIdProduit(), "-"));

        // Charge l’image (comme MarketplaceController)
        detailImageView.setImage(loadProductImage(p.getImagePath()));

        // Matériels associés
        List<Materiel> mats = materielDAO.getAllByProduit(p.getIdProduit());
        materielTable.setItems(FXCollections.observableArrayList(mats));
    }

    private void clearDetails() {
        detailTitleLabel.setText("Fiche produit");
        detailIdLabel.setText("-");
        detailNomLabel.setText("-");
        detailStockLabel.setText("-");
        detailUniteLabel.setText("-");
        detailExpLabel.setText("-");
        detailImagePathLabel.setText("-");
        matchInfoLabel.setText("-");
        detailImageView.setImage(null);
        materielTable.setItems(FXCollections.observableArrayList());
    }

    private Image loadProductImage(String path) {
        try {
            if (path != null && !path.isBlank()) {
                File file = new File(path);
                if (file.exists()) {
                    return new Image(file.toURI().toString(), true);
                }
                if (path.startsWith("http://") || path.startsWith("https://")) {
                    return new Image(path, true);
                }
                if (path.startsWith("/")) {
                    InputStream is = getClass().getResourceAsStream(path);
                    if (is != null) return new Image(is);
                }
            }
        } catch (Exception ignored) {}

        InputStream fallback = getClass().getResourceAsStream("/images/products/default.png");
        return fallback == null ? null : new Image(fallback);
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    private void showWarning(String header, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attention");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void showError(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private String normalizeText(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase(Locale.ROOT);
        t = t.replaceAll("[^a-z0-9à-ÿ]+", " ").trim();
        return t;
    }

    private String normalizePath(String p) {
        if (p == null) return "";
        return p.trim().replace("\\", "/").toLowerCase(Locale.ROOT);
    }

    private String stripExtension(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot <= 0 ? name : name.substring(0, dot);
    }

    private int simpleSimilarityScore(String a, String b) {
        if (a == null || b == null) return 0;
        String aa = normalizeText(a);
        String bb = normalizeText(b);
        if (aa.isEmpty() || bb.isEmpty()) return 0;
        if (aa.equals(bb)) return 40;
        if (aa.contains(bb) || bb.contains(aa)) return 25;

        // token overlap (petit score)
        Set<String> ta = new HashSet<>(Arrays.asList(aa.split("\\s+")));
        Set<String> tb = new HashSet<>(Arrays.asList(bb.split("\\s+")));
        ta.removeIf(String::isBlank);
        tb.removeIf(String::isBlank);
        if (ta.isEmpty() || tb.isEmpty()) return 0;
        ta.retainAll(tb);
        return Math.min(20, ta.size() * 5);
    }

    private String safeSha256(String path) {
        try {
            if (path == null || path.isBlank()) return null;
            File f = new File(path);
            if (!f.exists() || !f.isFile()) return null;

            String norm = normalizePath(f.getAbsolutePath());
            if (fileHashCache.containsKey(norm)) return fileHashCache.get(norm);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream is = new FileInputStream(f);
                 DigestInputStream dis = new DigestInputStream(is, md)) {
                byte[] buffer = new byte[8192];
                //noinspection StatementWithEmptyBody
                while (dis.read(buffer) != -1) { /* read stream */ }
            }
            String hex = bytesToHex(md.digest());
            fileHashCache.put(norm, hex);
            return hex;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static class Scored {
        final Produit produit;
        final int score;
        final String reason;

        private Scored(Produit produit, int score, String reason) {
            this.produit = produit;
            this.score = score;
            this.reason = reason;
        }
    }
}








package service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.Result;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.ReaderException;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Utilitaire pour g├®n├®rer et lire des QR codes li├®s aux produits.
 *
 * Format de contenu utilis├® : "PROD:{idProduit}"
 */
public class QrCodeService {

    private static final int QR_SIZE = 300;

    /**
     * G├®n├¿re un QR code pour un produit et le sauvegarde dans un dossier d├®di├®.
     *
     * @param idProduit identifiant du produit
     * @return chemin du fichier PNG g├®n├®r├®
     */
    public static Path generateProduitQr(int idProduit) throws IOException {
        String text = "PROD:" + idProduit;

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

            Path dir = getQrDirectory();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            Path output = dir.resolve("produit_" + idProduit + "_qr.png");
            MatrixToImageWriter.writeToPath(matrix, "PNG", output);
            return output;
        } catch (Exception e) {
            throw new IOException("Erreur lors de la g├®n├®ration du QR code", e);
        }
    }

    /**
     * Lit un fichier image suppos├® contenir un QR code et renvoie le texte d├®cod├®.
     *
     * @param imageFile fichier image
     * @return texte contenu dans le QR code ou null si non lisible
     */
    public static String decodeFromFile(File imageFile) throws IOException {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) return null;

            BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            MultiFormatReader reader = new MultiFormatReader();
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE));

            Result result = reader.decode(bitmap, hints);
            return result.getText();
        } catch (ReaderException e) {
            return null;
        }
    }

    /**
     * Dossier par d├®faut pour stocker les QR codes produits.
     */
    public static Path getQrDirectory() {
        String userHome = System.getProperty("user.home", ".");
        return Paths.get(userHome, "farmtech_qr_codes");
    }
}



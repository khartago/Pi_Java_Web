package Services;

import model.Probleme;
import Iservices.IProblemeService;
import Utils.Mydatabase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ProblemeService implements IProblemeService {

    Connection con;

    public ProblemeService() {
        con = Mydatabase.getInstance().getConnextion();
    }

    @Override
    public int ajouterProbleme(Probleme p) {

        String req = "INSERT INTO probleme (id_utilisateur, type, description, gravite, date_detection, etat, photos) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ste = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            if (p.getIdUtilisateur() != null) {
                ste.setInt(1, p.getIdUtilisateur());
            } else {
                ste.setNull(1, Types.INTEGER);
            }
            ste.setString(2, p.getType());
            ste.setString(3, p.getDescription());
            ste.setString(4, p.getGravite());
            ste.setTimestamp(5, Timestamp.valueOf(p.getDateDetection()));
            ste.setString(6, p.getEtat());
            ste.setString(7, p.getPhotos());
            ste.executeUpdate();

            ResultSet rs = ste.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                System.out.println("probleme ajoute id=" + id);
                return id;
            }
            throw new RuntimeException("Aucun ID généré pour le problème");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modifierProbleme(Probleme p) {

        String req = "UPDATE probleme SET id_utilisateur = ?, type = ?, description = ?, gravite = ?, date_detection = ?, etat = ?, photos = ? WHERE id = ?";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            if (p.getIdUtilisateur() != null) {
                ste.setInt(1, p.getIdUtilisateur());
            } else {
                ste.setNull(1, Types.INTEGER);
            }
            ste.setString(2, p.getType());
            ste.setString(3, p.getDescription());
            ste.setString(4, p.getGravite());
            ste.setTimestamp(5, Timestamp.valueOf(p.getDateDetection()));
            ste.setString(6, p.getEtat());
            ste.setString(7, p.getPhotos());
            ste.setInt(8, p.getId());
            ste.executeUpdate();
            System.out.println("probleme mis a jour");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void supprimerProbleme(int id) {

        String req = "DELETE FROM probleme WHERE id = ?";
        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setInt(1, id);
            ste.executeUpdate();
            System.out.println("probleme avec id = " + id + " supprime");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Probleme> afficherProblemes() {
        List<Probleme> problemes = new ArrayList<>();
        String req = "SELECT * FROM probleme";
        try {
            Statement ste = con.createStatement();
            ResultSet rs = ste.executeQuery(req);

            while (rs.next()) {
                Probleme p = mapRowToProbleme(rs);
                problemes.add(p);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return problemes;
    }

    @Override
    public List<Probleme> afficherProblemesParUtilisateur(int idUtilisateur) {
        List<Probleme> problemes = new ArrayList<>();
        String req = "SELECT * FROM probleme WHERE id_utilisateur = ? ORDER BY date_detection DESC";
        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setInt(1, idUtilisateur);
            ResultSet rs = ste.executeQuery();
            while (rs.next()) {
                problemes.add(mapRowToProbleme(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return problemes;
    }

    @Override
    public Probleme getProblemeById(int id) {
        String req = "SELECT * FROM probleme WHERE id = ?";
        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setInt(1, id);
            ResultSet rs = ste.executeQuery();
            if (rs.next()) {
                return mapRowToProbleme(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Probleme> afficherProblemesParEtat(String etat) {
        List<Probleme> problemes = new ArrayList<>();
        String req = "SELECT * FROM probleme WHERE etat = ?";
        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setString(1, etat);
            ResultSet rs = ste.executeQuery();

            while (rs.next()) {
                Probleme p = mapRowToProbleme(rs);
                problemes.add(p);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return problemes;
    }

    @Override
    public List<Probleme> afficherProblemesFiltresEtTries(String type, String gravite, String etat,
                                                          LocalDate dateDebut, LocalDate dateFin, String ordreTri) {
        List<Probleme> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM probleme WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (type != null && !type.isEmpty() && !"Tous".equals(type)) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        if (gravite != null && !gravite.isEmpty() && !"Tous".equals(gravite)) {
            sql.append(" AND gravite = ?");
            params.add(gravite);
        }
        if (etat != null && !etat.isEmpty() && !"Tous".equals(etat)) {
            sql.append(" AND etat = ?");
            params.add(etat);
        }
        if (dateDebut != null) {
            sql.append(" AND date_detection >= ?");
            params.add(java.sql.Timestamp.valueOf(dateDebut.atStartOfDay()));
        }
        if (dateFin != null) {
            sql.append(" AND date_detection < ?");
            params.add(java.sql.Timestamp.valueOf(dateFin.plusDays(1).atStartOfDay()));
        }

        if (ordreTri != null && !ordreTri.isEmpty()) {
            switch (ordreTri) {
                case "date_asc":
                    sql.append(" ORDER BY date_detection ASC");
                    break;
                case "gravite_desc":
                    sql.append(" ORDER BY FIELD(gravite, 'Critique', 'Élevée', 'Moyenne', 'Faible')");
                    break;
                case "type_asc":
                    sql.append(" ORDER BY type ASC");
                    break;
                case "date_desc":
                default:
                    sql.append(" ORDER BY date_detection DESC");
                    break;
            }
        } else {
            sql.append(" ORDER BY date_detection DESC");
        }

        try {
            PreparedStatement ste = con.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                Object o = params.get(i);
                if (o instanceof String) ste.setString(i + 1, (String) o);
                else if (o instanceof Timestamp) ste.setTimestamp(i + 1, (Timestamp) o);
            }
            ResultSet rs = ste.executeQuery();
            while (rs.next()) {
                list.add(mapRowToProbleme(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<String> getTypesDistincts() {
        List<String> types = new ArrayList<>();
        String req = "SELECT DISTINCT type FROM probleme ORDER BY type";
        try {
            Statement ste = con.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                types.add(rs.getString("type"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return types;
    }

    private Probleme mapRowToProbleme(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Integer idUtilisateur = null;
        try {
            int uid = rs.getInt("id_utilisateur");
            if (!rs.wasNull()) idUtilisateur = uid;
        } catch (SQLException ignored) { /* colonne absente en ancienne BDD */ }
        String type = rs.getString("type");
        String description = rs.getString("description");
        String gravite = rs.getString("gravite");
        Timestamp ts = rs.getTimestamp("date_detection");
        LocalDateTime dateDetection = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String etat = rs.getString("etat");
        String photos = rs.getString("photos");
        return new Probleme(id, idUtilisateur, type, description, gravite, dateDetection, etat, photos);
    }
}


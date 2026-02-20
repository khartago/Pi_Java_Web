package Services;

import model.Probleme;
import Iservices.IProblemeService;
import Utils.Mydatabase;

import java.sql.*;
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
    public void ajouterProbleme(Probleme p) {

        String req = "INSERT INTO probleme (type, description, gravite, date_detection, etat, photos) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setString(1, p.getType());
            ste.setString(2, p.getDescription());
            ste.setString(3, p.getGravite());
            ste.setTimestamp(4, Timestamp.valueOf(p.getDateDetection()));
            ste.setString(5, p.getEtat());
            ste.setString(6, p.getPhotos());
            ste.executeUpdate();

            System.out.println("probleme ajoute");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modifierProbleme(Probleme p) {

        String req = "UPDATE probleme SET type = ?, description = ?, gravite = ?, date_detection = ?, etat = ?, photos = ? WHERE id = ?";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setString(1, p.getType());
            ste.setString(2, p.getDescription());
            ste.setString(3, p.getGravite());
            ste.setTimestamp(4, Timestamp.valueOf(p.getDateDetection()));
            ste.setString(5, p.getEtat());
            ste.setString(6, p.getPhotos());
            ste.setInt(7, p.getId());
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

    private Probleme mapRowToProbleme(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String type = rs.getString("type");
        String description = rs.getString("description");
        String gravite = rs.getString("gravite");
        Timestamp ts = rs.getTimestamp("date_detection");
        LocalDateTime dateDetection = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String etat = rs.getString("etat");
        String photos = rs.getString("photos");

        return new Probleme(id, type, description, gravite, dateDetection, etat, photos);
    }
}


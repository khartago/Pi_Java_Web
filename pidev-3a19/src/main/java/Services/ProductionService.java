package Services;

import model.Production;
import model.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductionService {

    public void ajouter(Production p) throws SQLException {
        String req = "INSERT INTO plantation (nomPlant, variete, quantite, datePlante, saison) VALUES (?, ?, ?, ?, ?)";
        try (Connection cnx = DBConnection.getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, p.getNomPlant());
            ps.setString(2, p.getVariete());
            ps.setInt(3, p.getQuantite());
            ps.setDate(4, p.getDatePlante());
            ps.setString(5, p.getSaison());
            ps.executeUpdate();
        }
    }

    public void updateEtat(int id, String etat) {
        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("UPDATE plantation SET etat=? WHERE id=?")) {
            ps.setString(1, etat);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erreur updateEtat: " + e.getMessage());
        }
    }

    public void modifier(Production p) throws SQLException {
        String req = "UPDATE plantation SET nomPlant=?, variete=?, quantite=?, datePlante=?, saison=? WHERE id=?";
        try (Connection cnx = DBConnection.getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, p.getNomPlant());
            ps.setString(2, p.getVariete());
            ps.setInt(3, p.getQuantite());
            ps.setDate(4, p.getDatePlante());
            ps.setString(5, p.getSaison());
            ps.setInt(6, p.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement("DELETE FROM plantation WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Production> afficher() throws SQLException {
        List<Production> list = new ArrayList<>();
        try (Connection cnx = DBConnection.getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM plantation")) {
            while (rs.next()) {
                Production p = new Production();
                p.setId(rs.getInt("id"));
                p.setNomPlant(rs.getString("nomPlant"));
                p.setVariete(rs.getString("variete"));
                p.setQuantite(rs.getInt("quantite"));
                p.setDatePlante(rs.getDate("datePlante"));
                p.setSaison(rs.getString("saison"));
                p.setEtat(rs.getString("etat"));
                list.add(p);
            }
        }
        return list;
    }
}

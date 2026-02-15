package tn.esprit.services;

import tn.esprit.entities.Production;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductionService {

    private Connection cnx;

    public ProductionService() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    // ✅ ADD
    public void ajouter(Production p) throws SQLException {
        String req = "INSERT INTO plantation (nomPlant, variete, quantite, datePlante, saison) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, p.getNomPlant());
        ps.setString(2, p.getVariete());
        ps.setInt(3, p.getQuantite());
        ps.setDate(4, p.getDatePlante());
        ps.setString(5, p.getSaison());

        ps.executeUpdate();
        System.out.println("✅ Production ajoutée !");
    }
    public void updateEtat(int id, String etat) {
        String sql = "UPDATE plantation SET etat=? WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, etat);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("❌ Erreur updateEtat: " + e.getMessage());
        }
    }

    // ✅ UPDATE
    public void modifier(Production p) throws SQLException {
        String req = "UPDATE plantation SET nomPlant=?, variete=?, quantite=?, datePlante=?, saison=? WHERE id=?";

        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, p.getNomPlant());
        ps.setString(2, p.getVariete());
        ps.setInt(3, p.getQuantite());
        ps.setDate(4, p.getDatePlante());
        ps.setString(5, p.getSaison());
        ps.setInt(6, p.getId());

        ps.executeUpdate();
        System.out.println("✅ Production modifiée !");
    }

    // ✅ DELETE
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM plantation WHERE id=?";

        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);

        ps.executeUpdate();
        System.out.println("✅ Production supprimée !");
    }

    // ✅ SELECT ALL
    public List<Production> afficher() throws SQLException {
        List<Production> list = new ArrayList<>();

        String req = "SELECT * FROM plantation";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Production p = new Production();
            p.setId(rs.getInt("id"));
            p.setNomPlant(rs.getString("nomPlant"));
            p.setVariete(rs.getString("variete"));
            p.setQuantite(rs.getInt("quantite"));
            p.setDatePlante(rs.getDate("datePlante"));
            p.setSaison(rs.getString("saison"));

            // ✅ IMPORTANT
            p.setEtat(rs.getString("etat"));

            list.add(p);
        }

        return list;
    }

}

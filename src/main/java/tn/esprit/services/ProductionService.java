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
        String req = "INSERT INTO production (nomPlant, variete, quantite, datePlante, saison) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, p.getNomPlant());
        ps.setString(2, p.getVariete());
        ps.setInt(3, p.getQuantite());
        ps.setDate(4, p.getDatePlante());
        ps.setString(5, p.getSaison());

        ps.executeUpdate();
        System.out.println("✅ Production ajoutée !");
    }

    // ✅ UPDATE
    public void modifier(Production p) throws SQLException {
        String req = "UPDATE production SET nomPlant=?, variete=?, quantite=?, datePlante=?, saison=? WHERE id=?";

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
        String req = "DELETE FROM production WHERE id=?";

        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);

        ps.executeUpdate();
        System.out.println("✅ Production supprimée !");
    }

    // ✅ SELECT ALL
    public List<Production> afficher() throws SQLException {
        List<Production> list = new ArrayList<>();

        String req = "SELECT * FROM production";
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

            list.add(p);
        }

        return list;
    }
}

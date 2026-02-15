package tn.esprit.services;

import tn.esprit.entities.ProductionPlante;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductionPlanteService {

    private Connection cnx;

    public ProductionPlanteService() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    // ===================== ADD =====================
    public void ajouter(ProductionPlante p) {
        String sql = "INSERT INTO production (idProduction, quantiteProduite, dateRecolte, qualite, etat) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setInt(1, p.getIdProduction()); // FK = plantation.id
            ps.setFloat(2, p.getQuantiteProduite());
            ps.setDate(3, p.getDateRecolte());
            ps.setString(4, p.getQualite());
            ps.setString(5, p.getEtat());

            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("❌ Erreur ajouter ProductionPlant: " + e.getMessage());
        }
    }


    // ===================== UPDATE =====================
    public void modifier(ProductionPlante p) {
        String sql = "UPDATE production SET quantiteProduite=?, dateRecolte=?, qualite=?, etat=? WHERE idProduction=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setFloat(1, p.getQuantiteProduite());
            ps.setDate(2, p.getDateRecolte());
            ps.setString(3, p.getQualite());
            ps.setString(4, p.getEtat());
            ps.setInt(5, p.getIdProduction());

            ps.executeUpdate();
            System.out.println("✅ ProductionPlante modifiée !");

        } catch (SQLException e) {
            System.out.println("❌ Erreur modification ProductionPlante : " + e.getMessage());
        }
    }

    // ===================== DELETE =====================
    public void supprimer(int idProduction) {
        String sql = "DELETE FROM production WHERE idProduction=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, idProduction);
            ps.executeUpdate();

            System.out.println("✅ ProductionPlante supprimée !");

        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression ProductionPlante : " + e.getMessage());
        }
    }

    // ===================== READ =====================
    public List<ProductionPlante> afficher() {
        List<ProductionPlante> list = new ArrayList<>();
        String sql = "SELECT * FROM production";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                ProductionPlante p = new ProductionPlante(
                        rs.getInt("idProduction"),
                        rs.getFloat("quantiteProduite"),
                        rs.getDate("dateRecolte"),
                        rs.getString("qualite"),
                        rs.getString("etat")
                );

                list.add(p);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur affichage ProductionPlante : " + e.getMessage());
        }

        return list;
    }
}

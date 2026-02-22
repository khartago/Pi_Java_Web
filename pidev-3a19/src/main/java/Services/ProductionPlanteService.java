package Services;

import model.ProductionPlante;
import model.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductionPlanteService {

    public void ajouter(ProductionPlante p) {

        String sql = "INSERT INTO production "
                + "(idProduction, quantiteProduite, dateRecolte, qualite, etat) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, p.getIdProduction()); // 🔥 VERY IMPORTANT
            ps.setFloat(2, p.getQuantiteProduite());
            ps.setDate(3, p.getDateRecolte());
            ps.setString(4, p.getQualite());
            ps.setString(5, p.getEtat());

            ps.executeUpdate();

        } catch (Exception e) {
            System.err.println("Erreur ajouter ProductionPlante: " + e.getMessage());
        }
    }

    public void modifier(ProductionPlante p) {
        String sql = "UPDATE production SET quantiteProduite=?, dateRecolte=?, qualite=?, etat=? WHERE idProduction=?";
        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setFloat(1, p.getQuantiteProduite());
            ps.setDate(2, p.getDateRecolte());
            ps.setString(3, p.getQualite());
            ps.setString(4, p.getEtat());
            ps.setInt(5, p.getIdProduction());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur modification ProductionPlante: " + e.getMessage());
        }
    }

    public void supprimer(int idProduction) {
        String sql = "DELETE FROM production WHERE idProduction=?";
        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idProduction);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur suppression ProductionPlante: " + e.getMessage());
        }
    }

    public List<ProductionPlante> afficher() {
        List<ProductionPlante> list = new ArrayList<>();
        String sql = "SELECT * FROM production";
        try (Connection cnx = DBConnection.getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ProductionPlante(
                        rs.getInt("idProduction"),
                        rs.getFloat("quantiteProduite"),
                        rs.getDate("dateRecolte"),
                        rs.getString("qualite"),
                        rs.getString("etat")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur affichage ProductionPlante: " + e.getMessage());
        }
        return list;
    }
}

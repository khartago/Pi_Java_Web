package Services;

import model.Production;
import model.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductionService {

    public void ajouter(Production p) throws SQLException {

        String req = "INSERT INTO plantation " +
                "(nomPlant, variete, quantite, datePlante, saison, etat, stage, water_count, last_water_time, status, growth_speed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(req)) {

            ps.setString(1, p.getNomPlant());
            ps.setString(2, p.getVariete());
            ps.setInt(3, p.getQuantite());
            ps.setDate(4, p.getDatePlante());
            ps.setString(5, p.getSaison());

            // OLD
            ps.setString(6, p.getEtat());

            // NEW GAME FIELDS
            ps.setInt(7, p.getStage());
            ps.setInt(8, p.getWaterCount());
            ps.setLong(9, p.getLastWaterTime());
            ps.setString(10, p.getStatus());
            ps.setDouble(11, p.getGrowthSpeed());

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

        String req = "UPDATE plantation SET " +
                "nomPlant=?, variete=?, quantite=?, datePlante=?, saison=? WHERE id=?";

        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(req)) {

            ps.setString(1, p.getNomPlant());
            ps.setString(2, p.getVariete());
            ps.setInt(3, p.getQuantite());
            ps.setDate(4, p.getDatePlante());
            ps.setString(5, p.getSaison());
            ps.setInt(6, p.getId());

            ps.executeUpdate();
        }
    }
    public void waterPlant(int id) throws SQLException {

        String select = "SELECT water_count, stage, growth_speed FROM plantation WHERE id=?";
        String update = "UPDATE plantation SET water_count=?, stage=?, last_water_time=?, status=? WHERE id=?";

        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement psSelect = cnx.prepareStatement(select);
             PreparedStatement psUpdate = cnx.prepareStatement(update)) {

            psSelect.setInt(1, id);
            ResultSet rs = psSelect.executeQuery();

            if (rs.next()) {

                int waterCount = rs.getInt("water_count");
                int stage = rs.getInt("stage");
                double speed = rs.getDouble("growth_speed");

                waterCount++;

                // 🔥 STAGE LOGIC (3 stages)
                // every 2 waters = next stage (you can adjust)
                if (waterCount >= 2 && stage == 1) stage = 2;
                if (waterCount >= 4 && stage == 2) stage = 3;

                String status = "ALIVE";
                if (stage == 3) status = "READY";

                psUpdate.setInt(1, waterCount);
                psUpdate.setInt(2, stage);
                psUpdate.setLong(3, System.currentTimeMillis());
                psUpdate.setString(4, status);
                psUpdate.setInt(5, id);

                psUpdate.executeUpdate();
            }
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

                // NEW GAME FIELDS
                p.setStage(rs.getInt("stage"));
                p.setWaterCount(rs.getInt("water_count"));
                p.setLastWaterTime(rs.getLong("last_water_time"));
                p.setStatus(rs.getString("status"));
                p.setGrowthSpeed(rs.getDouble("growth_speed"));

                list.add(p);
            }
        }

        return list;
    }
}
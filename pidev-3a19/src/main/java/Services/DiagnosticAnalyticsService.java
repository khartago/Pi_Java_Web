package Services;

import Utils.Mydatabase;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service d'analytics pour les diagnostics : taux de résolution, types, causes, durée moyenne.
 */
public class DiagnosticAnalyticsService {

    private final Connection con;

    public DiagnosticAnalyticsService() {
        con = Mydatabase.getInstance().getConnextion();
    }

    /**
     * Taux de résolution = COUNT(feedback_fermier='RESOLU') / COUNT(diagnostique avec feedback).
     */
    public double getTauxResolution() {
        String req = "SELECT " +
                "SUM(CASE WHEN feedback_fermier = 'RESOLU' THEN 1 ELSE 0 END) AS resolus, " +
                "SUM(CASE WHEN feedback_fermier IN ('RESOLU','NON_RESOLU') THEN 1 ELSE 0 END) AS total_feedback " +
                "FROM diagnostique d WHERE d.approuve = 1";
        try (Statement ste = con.createStatement(); ResultSet rs = ste.executeQuery(req)) {
            if (rs.next()) {
                int total = rs.getInt("total_feedback");
                if (total == 0) return 0.0;
                return (double) rs.getInt("resolus") / total;
            }
        } catch (SQLException e) {
            // Colonne feedback_fermier peut être absente
            try {
                String fallback = "SELECT COUNT(*) AS c FROM diagnostique WHERE approuve = 1";
                try (Statement s = con.createStatement(); ResultSet r = s.executeQuery(fallback)) {
                    return r.next() && r.getInt("c") > 0 ? 0.0 : 0.0;
                }
            } catch (SQLException ignored) {
            }
        }
        return 0.0;
    }

    /**
     * Nombre de problèmes par type.
     */
    public Map<String, Long> getProblemesParType() {
        Map<String, Long> map = new LinkedHashMap<>();
        String req = "SELECT p.type, COUNT(*) AS cnt FROM probleme p GROUP BY p.type ORDER BY cnt DESC";
        try (Statement ste = con.createStatement(); ResultSet rs = ste.executeQuery(req)) {
            while (rs.next()) {
                map.put(rs.getString("type"), rs.getLong("cnt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /**
     * Causes les plus fréquentes (tronquées à 50 caractères pour regroupement).
     */
    public Map<String, Long> getCausesFrequentes() {
        Map<String, Long> map = new LinkedHashMap<>();
        String req = "SELECT COALESCE(SUBSTRING(d.cause, 1, 50), '') AS cause_trunc, COUNT(*) AS cnt " +
                "FROM diagnostique d WHERE d.approuve = 1 GROUP BY 1 ORDER BY cnt DESC LIMIT 10";
        try (Statement ste = con.createStatement(); ResultSet rs = ste.executeQuery(req)) {
            while (rs.next()) {
                map.put(rs.getString("cause_trunc"), rs.getLong("cnt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /**
     * Durée moyenne entre date_detection et date_diagnostique (en heures).
     */
    public double getDureeMoyenneDiagnostic() {
        String req = "SELECT AVG(TIMESTAMPDIFF(HOUR, p.date_detection, d.date_diagnostique)) AS avg_hours " +
                "FROM probleme p JOIN diagnostique d ON d.id_probleme = p.id WHERE d.approuve = 1";
        try (Statement ste = con.createStatement(); ResultSet rs = ste.executeQuery(req)) {
            if (rs.next()) {
                return rs.getDouble("avg_hours");
            }
        } catch (SQLException e) {
            // MySQL peut ne pas avoir TIMESTAMPDIFF selon la version
            try {
                String fallback = "SELECT AVG(UNIX_TIMESTAMP(d.date_diagnostique) - UNIX_TIMESTAMP(p.date_detection)) / 3600 AS avg_hours " +
                        "FROM probleme p JOIN diagnostique d ON d.id_probleme = p.id WHERE d.approuve = 1";
                try (Statement s = con.createStatement(); ResultSet r = s.executeQuery(fallback)) {
                    if (r.next()) return r.getDouble("avg_hours");
                }
            } catch (SQLException ignored) {
            }
        }
        return 0.0;
    }
}

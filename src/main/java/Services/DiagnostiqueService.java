package Services;

import model.Diagnostique;
import model.Probleme;
import Iservices.IDiagnostiqueService;
import Utils.Mydatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class DiagnostiqueService implements IDiagnostiqueService {

    Connection con;

    public DiagnostiqueService() {
        con = Mydatabase.getInstance().getConnextion();
    }

    @Override
    public void ajouterDiagnostique(Diagnostique d) {

        String req = "INSERT INTO diagnostique (id_probleme, cause, solution_proposee, date_diagnostique, resultat, medicament, approuve, num_revision, id_admin_diagnostiqueur) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            int numRev = d.getNumRevision() > 0 ? d.getNumRevision() : 1;
            PreparedStatement ste = con.prepareStatement(req);
            ste.setInt(1, d.getIdProbleme());
            ste.setString(2, d.getCause());
            ste.setString(3, d.getSolutionProposee());
            ste.setTimestamp(4, Timestamp.valueOf(d.getDateDiagnostique()));
            ste.setString(5, d.getResultat());
            ste.setString(6, d.getMedicament());
            ste.setBoolean(7, d.isApprouve());
            ste.setInt(8, numRev);
            if (d.getIdAdminDiagnostiqueur() != null) {
                ste.setInt(9, d.getIdAdminDiagnostiqueur());
            } else {
                ste.setNull(9, Types.INTEGER);
            }
            ste.executeUpdate();

            System.out.println("diagnostique ajoute");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modifierDiagnostique(Diagnostique d) {

        String req = "UPDATE diagnostique SET cause = ?, solution_proposee = ?, date_diagnostique = ?, resultat = ?, medicament = ?, approuve = ?, id_admin_diagnostiqueur = ? WHERE id = ?";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setString(1, d.getCause());
            ste.setString(2, d.getSolutionProposee());
            ste.setTimestamp(3, Timestamp.valueOf(d.getDateDiagnostique()));
            ste.setString(4, d.getResultat());
            ste.setString(5, d.getMedicament());
            ste.setBoolean(6, d.isApprouve());
            if (d.getIdAdminDiagnostiqueur() != null) {
                ste.setInt(7, d.getIdAdminDiagnostiqueur());
            } else {
                ste.setNull(7, Types.INTEGER);
            }
            ste.setInt(8, d.getId());
            ste.executeUpdate();

            System.out.println("diagnostique mis a jour");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void supprimerDiagnostique(int id) {

        String req = "DELETE FROM diagnostique WHERE id = ?";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setInt(1, id);
            ste.executeUpdate();

            System.out.println("diagnostique avec id = " + id + " supprime");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Diagnostique> afficherDiagnostiques() {
        List<Diagnostique> diagnostiques = new ArrayList<>();
        String req = "SELECT * FROM diagnostique";

        try {
            Statement ste = con.createStatement();
            ResultSet rs = ste.executeQuery(req);

            while (rs.next()) {
                Diagnostique d = mapRowToDiagnostique(rs);
                diagnostiques.add(d);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return diagnostiques;
    }

    @Override
    public Diagnostique afficherDiagnostiqueParProbleme(int idProbleme) {
        String req = "SELECT * FROM diagnostique WHERE id_probleme = ? ORDER BY num_revision DESC LIMIT 1";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setInt(1, idProbleme);
            ResultSet rs = ste.executeQuery();

            if (rs.next()) {
                return mapRowToDiagnostique(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public Diagnostique afficherDiagnostiqueParProblemeApprouve(int idProbleme) {
        return getDiagnostiqueActif(idProbleme);
    }

    /** Retourne le dernier diagnostic approuvé (révision la plus récente visible au fermier). */
    public Diagnostique getDiagnostiqueActif(int idProbleme) {
        String req = "SELECT * FROM diagnostique WHERE id_probleme = ? AND approuve = 1 ORDER BY num_revision DESC LIMIT 1";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setInt(1, idProbleme);
            ResultSet rs = ste.executeQuery();

            if (rs.next()) {
                return mapRowToDiagnostique(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    /** Retourne des diagnostics similaires (même type, approuvés) pour aider l'admin. */
    public List<Diagnostique> getDiagnostiquesSimilaires(String type, int idProblemeExclu, int limit) {
        List<Diagnostique> list = new ArrayList<>();
        String req = "SELECT d.* FROM diagnostique d JOIN probleme p ON d.id_probleme = p.id " +
                "WHERE p.type = ? AND d.approuve = 1 AND p.id != ? ORDER BY d.date_diagnostique DESC LIMIT ?";
        try (PreparedStatement ste = con.prepareStatement(req)) {
            ste.setString(1, type);
            ste.setInt(2, idProblemeExclu);
            ste.setInt(3, Math.max(1, Math.min(limit, 10)));
            ResultSet rs = ste.executeQuery();
            while (rs.next()) {
                list.add(mapRowToDiagnostique(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /** Retourne tous les diagnostics d'un problème (pour afficher l'historique des révisions). */
    public List<Diagnostique> getDiagnostiquesParProbleme(int idProbleme) {
        List<Diagnostique> list = new ArrayList<>();
        String req = "SELECT * FROM diagnostique WHERE id_probleme = ? ORDER BY num_revision ASC";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setInt(1, idProbleme);
            ResultSet rs = ste.executeQuery();

            while (rs.next()) {
                list.add(mapRowToDiagnostique(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    /** Crée une nouvelle révision de diagnostic (après réouverture). */
    public void creerRevision(int idProbleme, Diagnostique nouveau) {
        int numRev = getMaxRevision(idProbleme) + 1;
        nouveau.setNumRevision(numRev);
        nouveau.setIdProbleme(idProbleme);
        nouveau.setApprouve(false);
        ajouterDiagnostique(nouveau);
    }

    private int getMaxRevision(int idProbleme) {
        String req = "SELECT COALESCE(MAX(num_revision), 0) AS m FROM diagnostique WHERE id_probleme = ?";
        try (PreparedStatement ste = con.prepareStatement(req)) {
            ste.setInt(1, idProbleme);
            ResultSet rs = ste.executeQuery();
            if (rs.next()) {
                return rs.getInt("m");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    /** Valeur d'état du problème lorsque le diagnostic est visible par le fermier. */
    public static final String ETAT_DIAGNOSTIQUE_DISPONIBLE = "DIAGNOSTIQUE_DISPONIBLE";

    @Override
    public void approuverDiagnostique(int idDiagnostique) {
        try {
            int idProbleme;
            try (PreparedStatement sel = con.prepareStatement("SELECT id_probleme FROM diagnostique WHERE id = ?")) {
                sel.setInt(1, idDiagnostique);
                ResultSet rs = sel.executeQuery();
                if (!rs.next()) {
                    throw new RuntimeException("Diagnostique introuvable: id=" + idDiagnostique);
                }
                idProbleme = rs.getInt("id_probleme");
            }
            try (PreparedStatement ste = con.prepareStatement("UPDATE diagnostique SET approuve = 1 WHERE id = ?")) {
                ste.setInt(1, idDiagnostique);
                ste.executeUpdate();
            }
            try (PreparedStatement upd = con.prepareStatement("UPDATE probleme SET etat = ? WHERE id = ?")) {
                upd.setString(1, ETAT_DIAGNOSTIQUE_DISPONIBLE);
                upd.setInt(2, idProbleme);
                upd.executeUpdate();
            }
            sendDiagnosticApprovedEmail(idProbleme, idDiagnostique);
            System.out.println("diagnostique id=" + idDiagnostique + " approuve, probleme id=" + idProbleme + " etat=" + ETAT_DIAGNOSTIQUE_DISPONIBLE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendDiagnosticApprovedEmail(int idProbleme, int idDiagnostique) {
        try {
            Probleme probleme = new ProblemeService().getProblemeById(idProbleme);
            Diagnostique diagnostique = getDiagnostiqueById(idDiagnostique);
            if (probleme == null || diagnostique == null) return;
            if (probleme.getIdUtilisateur() == null) return;

            String nom = null;
            try (PreparedStatement ps = con.prepareStatement("SELECT nom FROM utilisateur WHERE id = ?")) {
                ps.setInt(1, probleme.getIdUtilisateur());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    nom = rs.getString("nom");
                }
            }
            if (nom == null) nom = "Utilisateur";

            String html = EmailTemplates.buildDiagnosticApprovedHtml(probleme, diagnostique, nom);
            String plain = EmailTemplates.buildDiagnosticApprovedPlainText(probleme, diagnostique, nom);
            new EmailService().sendHtmlEmail("rayen0799@gmail.com", "FARMTECH – Votre diagnostic est disponible", html, plain);
        } catch (Exception e) {
            System.err.println("Erreur envoi email diagnostic: " + e.getMessage());
        }
    }

    private Diagnostique getDiagnostiqueById(int id) {
        String req = "SELECT * FROM diagnostique WHERE id = ?";
        try (PreparedStatement ste = con.prepareStatement(req)) {
            ste.setInt(1, id);
            ResultSet rs = ste.executeQuery();
            if (rs.next()) return mapRowToDiagnostique(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private Diagnostique mapRowToDiagnostique(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int idProbleme = rs.getInt("id_probleme");
        String cause = rs.getString("cause");
        String solutionProposee = rs.getString("solution_proposee");
        Timestamp ts = rs.getTimestamp("date_diagnostique");
        LocalDateTime dateDiagnostique = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String resultat = rs.getString("resultat");
        String medicament = rs.getString("medicament");
        boolean approuve = false;
        try {
            approuve = rs.getBoolean("approuve");
        } catch (SQLException ignored) {
        }

        Diagnostique d = new Diagnostique(id, idProbleme, cause, solutionProposee, dateDiagnostique, resultat, medicament, approuve);
        try {
            d.setNumRevision(rs.getInt("num_revision"));
        } catch (SQLException ignored) {
        }
        try {
            int aid = rs.getInt("id_admin_diagnostiqueur");
            if (!rs.wasNull()) d.setIdAdminDiagnostiqueur(aid);
        } catch (SQLException ignored) {
        }
        try {
            d.setFeedbackFermier(rs.getString("feedback_fermier"));
            d.setFeedbackCommentaire(rs.getString("feedback_commentaire"));
            Timestamp tsFeedback = rs.getTimestamp("date_feedback");
            if (tsFeedback != null) {
                d.setDateFeedback(tsFeedback.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
        } catch (SQLException ignored) {
        }
        return d;
    }

    /** Valeurs possibles pour feedback_fermier */
    public static final String FEEDBACK_RESOLU = "RESOLU";
    public static final String FEEDBACK_NON_RESOLU = "NON_RESOLU";

    /** Met à jour le feedback du fermier et, si RESOLU, clôture le problème. */
    public void enregistrerFeedback(int idDiagnostique, String resolu, String commentaire) {
        try {
            int idProbleme;
            try (PreparedStatement sel = con.prepareStatement("SELECT id_probleme FROM diagnostique WHERE id = ?")) {
                sel.setInt(1, idDiagnostique);
                ResultSet rs = sel.executeQuery();
                if (!rs.next()) {
                    throw new RuntimeException("Diagnostique introuvable: id=" + idDiagnostique);
                }
                idProbleme = rs.getInt("id_probleme");
            }
            try (PreparedStatement upd = con.prepareStatement("UPDATE diagnostique SET feedback_fermier = ?, feedback_commentaire = ?, date_feedback = ? WHERE id = ?")) {
                upd.setString(1, resolu);
                upd.setString(2, commentaire);
                upd.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                upd.setInt(4, idDiagnostique);
                upd.executeUpdate();
            }
            if (FEEDBACK_RESOLU.equals(resolu)) {
                try (PreparedStatement upd = con.prepareStatement("UPDATE probleme SET etat = ? WHERE id = ?")) {
                    upd.setString(1, "CLOTURE");
                    upd.setInt(2, idProbleme);
                    upd.executeUpdate();
                }
            } else if (FEEDBACK_NON_RESOLU.equals(resolu)) {
                try (PreparedStatement upd = con.prepareStatement("UPDATE probleme SET etat = ? WHERE id = ?")) {
                    upd.setString(1, "REOUVERT");
                    upd.setInt(2, idProbleme);
                    upd.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}


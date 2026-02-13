package Services;

import Entites.Diagnostique;
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

        String req = "INSERT INTO diagnostique (id_probleme, cause, solution_proposee, date_diagnostique, resultat) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setInt(1, d.getIdProbleme());
            ste.setString(2, d.getCause());
            ste.setString(3, d.getSolutionProposee());
            ste.setTimestamp(4, Timestamp.valueOf(d.getDateDiagnostique()));
            ste.setString(5, d.getResultat());
            ste.executeUpdate();

            System.out.println("diagnostique ajoute");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modifierDiagnostique(Diagnostique d) {

        String req = "UPDATE diagnostique SET cause = ?, solution_proposee = ?, date_diagnostique = ?, resultat = ? WHERE id = ?";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setString(1, d.getCause());
            ste.setString(2, d.getSolutionProposee());
            ste.setTimestamp(3, Timestamp.valueOf(d.getDateDiagnostique()));
            ste.setString(4, d.getResultat());
            ste.setInt(5, d.getId());
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
        String req = "SELECT * FROM diagnostique WHERE id_probleme = ? LIMIT 1";

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

    private Diagnostique mapRowToDiagnostique(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int idProbleme = rs.getInt("id_probleme");
        String cause = rs.getString("cause");
        String solutionProposee = rs.getString("solution_proposee");
        Timestamp ts = rs.getTimestamp("date_diagnostique");
        LocalDateTime dateDiagnostique = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String resultat = rs.getString("resultat");

        return new Diagnostique(id, idProbleme, cause, solutionProposee, dateDiagnostique, resultat);
    }
}


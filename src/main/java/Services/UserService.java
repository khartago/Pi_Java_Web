package Services;

import model.User;
import Iservices.IUserService;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IUserService {

    Connection con;

    public UserService() {
        con = Mydatabase.getInstance().getConnextion();
    }

    @Override
    public void ajouterUser(User u) {
        String req = "INSERT INTO utilisateur (nom, email, mot_de_passe, role) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setString(1, u.getNom());
            ste.setString(2, u.getEmail());
            ste.setString(3, u.getMotDePasse());
            ste.setString(4, u.getRole());
            ste.executeUpdate();
            System.out.println("user ajoute");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modifierUser(User u) {
        String req = "UPDATE utilisateur SET nom = ?, email = ?, mot_de_passe = ?, role = ? WHERE id = ?";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setString(1, u.getNom());
            ste.setString(2, u.getEmail());
            ste.setString(3, u.getMotDePasse());
            ste.setString(4, u.getRole());
            ste.setInt(5, u.getId());
            ste.executeUpdate();
            System.out.println("user mis a jour");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void supprimerUser(int id) {
        String req = "DELETE FROM utilisateur WHERE id = ?";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setInt(1, id);
            ste.executeUpdate();
            System.out.println("user avec id = " + id + " supprime");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> afficherUsers() {
        List<User> users = new ArrayList<>();
        String req = "SELECT * FROM utilisateur";

        try {
            Statement ste = con.createStatement();
            ResultSet rs = ste.executeQuery(req);

            while (rs.next()) {
                User u = mapRowToUser(rs);
                users.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return users;
    }

    @Override
    public User trouverParEmail(String email) {
        String req = "SELECT * FROM utilisateur WHERE email = ?";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setString(1, email);
            ResultSet rs = ste.executeQuery();

            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public User authentifier(String email, String motDePasse) {
        String req = "SELECT * FROM utilisateur WHERE email = ? AND mot_de_passe = ?";

        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setString(1, email);
            ste.setString(2, motDePasse);
            ResultSet rs = ste.executeQuery();

            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    /** Retourne les utilisateurs avec role ADMIN. */
    public List<User> getAdmins() {
        List<User> list = new ArrayList<>();
        String req = "SELECT * FROM utilisateur WHERE role = 'ADMIN' ORDER BY nom";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public String getNomById(int id) {
        String req = "SELECT nom FROM utilisateur WHERE id = ?";
        try (PreparedStatement ste = con.prepareStatement(req)) {
            ste.setInt(1, id);
            ResultSet rs = ste.executeQuery();
            if (rs.next()) return rs.getString("nom");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nom = rs.getString("nom");
        String email = rs.getString("email");
        String motDePasse = rs.getString("mot_de_passe");
        String role = rs.getString("role");

        return new User(id, nom, email, motDePasse, role);
    }
}


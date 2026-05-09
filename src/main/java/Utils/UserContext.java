package Utils;

import model.User;

/**
 * Utilisateur connecté (fermier ou admin). Renseigné au login et effacé au logout.
 */
public final class UserContext {

    private static User currentUser;

    private UserContext() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }
}

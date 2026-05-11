package Utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Hash and verify passwords compatible with Symfony's bcrypt hasher (PHP {@code password_hash}).
 */
public final class BcryptPasswords {

    private static final int COST = 12;

    private BcryptPasswords() {
    }

    public static String hash(String plain) {
        return BCrypt.withDefaults().hashToString(COST, plain.toCharArray());
    }

    public static boolean looksLikeBcryptHash(String stored) {
        return stored != null && stored.length() > 3 && stored.startsWith("$2");
    }

    public static boolean verify(String plain, String stored) {
        if (stored == null || plain == null) {
            return false;
        }
        if (looksLikeBcryptHash(stored)) {
            return BCrypt.verifyer().verify(plain.toCharArray(), stored).verified;
        }
        return plain.equals(stored);
    }
}

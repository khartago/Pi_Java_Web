package Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * JDBC settings shared by {@link Mydatabase} and {@link model.DBConnection}.
 * Optional {@code database.properties} on the classpath overrides defaults (see {@code database.properties.example}).
 */
public final class DatabaseConfig {

    private static final String DEFAULT_URL =
            "jdbc:mysql://127.0.0.1:3306/3a19?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static final String JDBC_URL;
    private static final String JDBC_USER;
    private static final String JDBC_PASSWORD;

    static {
        Properties p = new Properties();
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        JDBC_URL = p.getProperty("jdbc.url", DEFAULT_URL);
        JDBC_USER = p.getProperty("jdbc.user", DEFAULT_USER);
        JDBC_PASSWORD = p.getProperty("jdbc.password", DEFAULT_PASSWORD);
    }

    private DatabaseConfig() {
    }

    public static String getJdbcUrl() {
        return JDBC_URL;
    }

    public static String getUser() {
        return JDBC_USER;
    }

    public static String getPassword() {
        return JDBC_PASSWORD;
    }
}

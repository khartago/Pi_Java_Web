package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class responsible for providing a JDBC connection to the underlying MySQL database.
 *
 * <p>The connection parameters (URL, user and password) are defined as constants at the top of
 * this class. Adapt these values to match your local database configuration. By default the
 * database is expected to run on the same machine (localhost) with the default MySQL port
 * (3306) and a schema named {@code agriculture_db}. The default user is {@code root} with an
 * empty password. If you run MySQL through XAMPP/phpMyAdmin the credentials might differ,
 * so please adjust them accordingly.</p>
 */
public class DBConnection {
    /** JDBC URL pointing at the database. */
    private static final String URL = "jdbc:mysql://localhost:3306/agriculture_db?useSSL=false&serverTimezone=UTC";
    /** Database user name. */
    private static final String USER = "root";
    /** Database password (empty by default). */
    private static final String PASSWORD = "";

    private DBConnection() {
        // utility class, no public constructor
    }

    /**
     * Creates and returns a new {@link Connection} to the MySQL database. Each call returns
     * an independent connection. Remember to close the connection after you are done with it
     * to free the underlying resources.
     *
     * @return a new {@code Connection} object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

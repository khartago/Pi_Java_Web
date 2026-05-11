package model;

import Utils.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class responsible for providing a JDBC connection to the underlying MySQL database.
 *
 * <p>Parameters are loaded from {@link Utils.DatabaseConfig} (optional {@code database.properties}
 * on the classpath; defaults match {@code farmtech-web} {@code DATABASE_URL} schema {@code 3a19}).</p>
 */
public class DBConnection {

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
        return DriverManager.getConnection(
                DatabaseConfig.getJdbcUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword());
    }
}

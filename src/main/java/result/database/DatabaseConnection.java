package result.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager responsible for establishing
 * and providing JDBC connections to the MySQL database.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/student_result_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=2000&socketTimeout=5000";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "Mohit@2026";

    private String url;
    private String user;
    private String password;

    static {
        // Explicitly register MySQL driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // Driver may already be on module path or classpath
        }
    }

    private DatabaseConnection() {
        loadConfiguration();
    }

    public synchronized void loadConfiguration() {
        this.url = DEFAULT_URL;
        this.user = DEFAULT_USER;
        this.password = DEFAULT_PASSWORD;

        Properties props = new Properties();
        boolean loaded = false;

        // 1. Try local file in current working directory
        File localFile = new File("database.properties");
        if (localFile.exists() && localFile.isFile()) {
            try (InputStream fis = new FileInputStream(localFile)) {
                props.load(fis);
                loaded = true;
            } catch (Exception ignored) {
            }
        }

        // 2. Try file in src/main/resources (development environment)
        if (!loaded) {
            File srcFile = new File("src/main/resources/database.properties");
            if (srcFile.exists() && srcFile.isFile()) {
                try (InputStream fis = new FileInputStream(srcFile)) {
                    props.load(fis);
                    loaded = true;
                } catch (Exception ignored) {
                }
            }
        }

        // 3. Try classpath resource
        if (!loaded) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("database.properties")) {
                if (in != null) {
                    props.load(in);
                    loaded = true;
                }
            } catch (Exception ignored) {
            }
        }

        if (loaded) {
            this.url = props.getProperty("db.url", DEFAULT_URL);
            this.user = props.getProperty("db.username", DEFAULT_USER);
            this.password = props.getProperty("db.password", DEFAULT_PASSWORD);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (result.ui.MainApplication.isBrowserEnvironment()) {
            throw new SQLException("Direct TCP raw socket connection to MySQL (port 3306) is blocked by browser WebAssembly security sandbox.");
        }
        return DriverManager.getConnection(url, user, password);
    }

    public Connection getRootServerConnection() throws SQLException {
        if (result.ui.MainApplication.isBrowserEnvironment()) {
            throw new SQLException("Direct TCP raw socket connection to MySQL (port 3306) is blocked by browser WebAssembly security sandbox.");
        }
        String baseServerUrl = url.replaceFirst("/student_result_db(\\?|$)", "/$1");
        return DriverManager.getConnection(baseServerUrl, user, password);
    }

    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // Suppressed for clean resource cleanup
            }
        }
    }
}

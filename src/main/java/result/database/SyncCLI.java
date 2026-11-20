package result.database;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;

public class SyncCLI {
    public static void main(String[] args) {
        String filePath = args != null && args.length > 0 ? args[0] : "web/srms_data.json";
        File file = new File(filePath);
        if (!file.exists()) {
            file = new File("srms_data.json");
        }
        if (!file.exists() || (args != null && args.length > 0 && "--export".equalsIgnoreCase(args[0]))) {
            System.out.println("[SyncCLI] Exporting live MySQL data to " + filePath + "...");
            try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                DataSyncBridge.exportMySQLToDisk(conn);
                System.out.println("[SyncCLI] SUCCESS: Exported MySQL data to disk.");
                return;
            } catch (Throwable t) {
                System.out.println("[SyncCLI] Exporting from InMemoryDataStore to disk...");
                DataSyncBridge.exportInMemoryToDisk();
                System.out.println("[SyncCLI] SUCCESS: Exported in-memory data to disk.");
                return;
            }
        }

        try {
            DatabaseInitializer.initializeDatabase();
            System.out.println("[SyncCLI] Syncing data from " + file.getPath() + " into MySQL...");
            DataSyncBridge.loadFromDisk();
            try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                DataSyncBridge.syncDiskToMySQL(conn);
                System.out.println("[SyncCLI] SUCCESS: MySQL updated with latest student data.");
            }
        } catch (Throwable t) {
            System.err.println("[SyncCLI] Error: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}

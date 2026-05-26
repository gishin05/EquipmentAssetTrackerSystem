package tracker.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:tracker.db";

    static {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        String createCategoriesTable = "CREATE TABLE IF NOT EXISTS categories (" +
                "category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category_name TEXT NOT NULL UNIQUE" +
                ");";

        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password_hash TEXT NOT NULL, " +
                "user_role TEXT NOT NULL" +
                ");";

        String createEquipmentTable = "CREATE TABLE IF NOT EXISTS equipment (" +
                "equipment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "serial_number TEXT NOT NULL UNIQUE, " +
                "category_id INTEGER, " +
                "technical_specifications TEXT, " +
                "storage_location TEXT, " +
                "purchase_cost REAL, " +
                "purchase_date TEXT, " +
                "equipment_status TEXT, " +
                "assigned_to INTEGER, " +
                "FOREIGN KEY(category_id) REFERENCES categories(category_id), " +
                "FOREIGN KEY(assigned_to) REFERENCES users(user_id)" +
                ");";

        String createBookingsTable = "CREATE TABLE IF NOT EXISTS bookings (" +
                "booking_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "equipment_id INTEGER, " +
                "borrower_id INTEGER, " +
                "admin_id INTEGER, " +
                "start_datetime TEXT, " +
                "expected_return_datetime TEXT, " +
                "actual_return_datetime TEXT, " +
                "purpose_description TEXT, " +
                "booking_status TEXT, " +
                "approval_status TEXT, " +
                "rejection_reason TEXT, " +
                "returned_condition TEXT, " +
                "borrowing_price REAL, " +
                "FOREIGN KEY(equipment_id) REFERENCES equipment(equipment_id), " +
                "FOREIGN KEY(borrower_id) REFERENCES users(user_id), " +
                "FOREIGN KEY(admin_id) REFERENCES users(user_id)" +
                ");";

        String createMaintenanceLogsTable = "CREATE TABLE IF NOT EXISTS maintenance_logs (" +
                "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "equipment_id INTEGER, " +
                "defect_description TEXT, " +
                "parts_cost REAL, " +
                "technician_details TEXT, " +
                "start_date TEXT, " +
                "completion_date TEXT, " +
                "repair_status TEXT, " +
                "FOREIGN KEY(equipment_id) REFERENCES equipment(equipment_id)" +
                ");";

        String createAuditLogsTable = "CREATE TABLE IF NOT EXISTS audit_logs (" +
                "audit_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "action_type TEXT, " +
                "affected_table TEXT, " +
                "record_id INTEGER, " +
                "action_timestamp TEXT, " +
                "user_id INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES users(user_id)" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createCategoriesTable);
            stmt.execute(createUsersTable);
            stmt.execute(createEquipmentTable);
            stmt.execute(createBookingsTable);
            stmt.execute(createMaintenanceLogsTable);
            stmt.execute(createAuditLogsTable);
            
            insertDefaultAdmin(conn);
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void insertDefaultAdmin(Connection conn) {
        String checkAdmin = "SELECT COUNT(*) FROM users WHERE username = 'Admin'";
        String insertAdmin = "INSERT INTO users (username, password_hash, user_role) VALUES (?, ?, ?)";
        String updateOldAdmin = "UPDATE users SET username = 'Admin', password_hash = ? WHERE username = 'admin'";
        
        try (Statement stmt = conn.createStatement()) {
            // Upgrade old 'admin' to 'Admin' if they already ran the app
            try (PreparedStatement updateStmt = conn.prepareStatement(updateOldAdmin)) {
                updateStmt.setString(1, "c1c224b03cd9bc7b6a86d77f5dace40191766c485cd55dc48caf9ac873335d6f");
                updateStmt.executeUpdate();
            }

            try (java.sql.ResultSet rs = stmt.executeQuery(checkAdmin)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement pstmt = conn.prepareStatement(insertAdmin)) {
                        pstmt.setString(1, "Admin");
                        // SHA-256 hash for 'Admin'
                        pstmt.setString(2, "c1c224b03cd9bc7b6a86d77f5dace40191766c485cd55dc48caf9ac873335d6f");
                        pstmt.setString(3, "ADMIN");
                        pstmt.executeUpdate();
                        System.out.println("Default admin user created.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

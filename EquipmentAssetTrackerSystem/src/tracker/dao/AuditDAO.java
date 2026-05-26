package tracker.dao;

import tracker.db.DatabaseManager;
import tracker.models.AuditLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {
    public List<AuditLog> getAllAuditLogs() {
        List<AuditLog> list = new ArrayList<>();
        String query = "SELECT * FROM audit_logs ORDER BY audit_id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                AuditLog a = new AuditLog();
                a.setAuditId(rs.getInt("audit_id"));
                a.setActionType(rs.getString("action_type"));
                a.setAffectedTable(rs.getString("affected_table"));
                a.setRecordId(rs.getInt("record_id"));
                a.setActionTimestamp(rs.getString("action_timestamp"));
                a.setUserId(rs.getInt("user_id"));
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean logAction(String actionType, String affectedTable, int recordId, int userId) {
        String query = "INSERT INTO audit_logs (action_type, affected_table, record_id, action_timestamp, user_id) VALUES (?, ?, ?, datetime('now'), ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, actionType);
            pstmt.setString(2, affectedTable);
            pstmt.setInt(3, recordId);
            pstmt.setInt(4, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

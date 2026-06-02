package tracker.dao;

import tracker.db.DatabaseManager;
import tracker.models.ChangeLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for change_logs table.
 * Records field-level changes to Inventory and Booking tables.
 */
public class ChangeLogDAO {

    /**
     * Retrieves all change logs for a specific table (Inventory or Booking),
     * ordered by most recent first. Joins with users table to get the username.
     */
    public List<ChangeLog> getChangeLogsByTable(String tableName) {
        List<ChangeLog> list = new ArrayList<>();
        String query = "SELECT cl.*, u.username FROM change_logs cl " +
                       "LEFT JOIN users u ON cl.user_id = u.user_id " +
                       "WHERE cl.table_name = ? " +
                       "ORDER BY cl.change_id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, tableName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ChangeLog cl = new ChangeLog();
                    cl.setChangeId(rs.getInt("change_id"));
                    cl.setTableName(rs.getString("table_name"));
                    cl.setRecordId(rs.getInt("record_id"));
                    cl.setRecordName(rs.getString("record_name"));
                    cl.setFieldName(rs.getString("field_name"));
                    cl.setOldValue(rs.getString("old_value"));
                    cl.setNewValue(rs.getString("new_value"));
                    cl.setChangeTimestamp(rs.getString("change_timestamp"));
                    cl.setUserId(rs.getInt("user_id"));
                    cl.setUsername(rs.getString("username"));
                    list.add(cl);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Logs a single field change.
     */
    public boolean logChange(String tableName, int recordId, String recordName,
                             String fieldName, String oldValue, String newValue, int userId) {
        String query = "INSERT INTO change_logs (table_name, record_id, record_name, field_name, " +
                       "old_value, new_value, change_timestamp, user_id) " +
                       "VALUES (?, ?, ?, ?, ?, ?, datetime('now', 'localtime'), ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, tableName);
            pstmt.setInt(2, recordId);
            pstmt.setString(3, recordName);
            pstmt.setString(4, fieldName);
            pstmt.setString(5, oldValue);
            pstmt.setString(6, newValue);
            pstmt.setInt(7, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Convenience: logs multiple field changes at once (e.g. when an equipment record is edited).
     * Only logs fields that actually changed.
     */
    public void logChanges(String tableName, int recordId, String recordName,
                           String[][] changes, int userId) {
        for (String[] change : changes) {
            // change[0] = fieldName, change[1] = oldValue, change[2] = newValue
            String oldVal = change[1] != null ? change[1] : "";
            String newVal = change[2] != null ? change[2] : "";
            if (!oldVal.equals(newVal)) {
                logChange(tableName, recordId, recordName, change[0], oldVal, newVal, userId);
            }
        }
    }
}

package tracker.dao;

import tracker.db.DatabaseManager;
import tracker.models.MaintenanceLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDAO {
    public List<MaintenanceLog> getAllMaintenanceLogs() {
        List<MaintenanceLog> list = new ArrayList<>();
        String query = "SELECT * FROM maintenance_logs";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                MaintenanceLog m = new MaintenanceLog();
                m.setLogId(rs.getInt("log_id"));
                m.setEquipmentId(rs.getInt("equipment_id"));
                m.setDefectDescription(rs.getString("defect_description"));
                m.setPartsCost(rs.getDouble("parts_cost"));
                m.setTechnicianDetails(rs.getString("technician_details"));
                m.setStartDate(rs.getString("start_date"));
                m.setCompletionDate(rs.getString("completion_date"));
                m.setRepairStatus(rs.getString("repair_status"));
                list.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addMaintenanceLog(MaintenanceLog log) {
        String query = "INSERT INTO maintenance_logs (equipment_id, defect_description, parts_cost, technician_details, start_date, repair_status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, log.getEquipmentId());
            pstmt.setString(2, log.getDefectDescription());
            pstmt.setDouble(3, log.getPartsCost());
            pstmt.setString(4, log.getTechnicianDetails());
            pstmt.setString(5, log.getStartDate());
            pstmt.setString(6, log.getRepairStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateMaintenanceLog(MaintenanceLog log) {
        String query = "UPDATE maintenance_logs SET equipment_id = ?, defect_description = ?, parts_cost = ?, technician_details = ?, start_date = ?, completion_date = ?, repair_status = ? WHERE log_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, log.getEquipmentId());
            pstmt.setString(2, log.getDefectDescription());
            pstmt.setDouble(3, log.getPartsCost());
            pstmt.setString(4, log.getTechnicianDetails());
            pstmt.setString(5, log.getStartDate());
            pstmt.setString(6, log.getCompletionDate());
            pstmt.setString(7, log.getRepairStatus());
            pstmt.setInt(8, log.getLogId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

package tracker.dao;

import tracker.db.DatabaseManager;
import tracker.models.Equipment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDAO {
    
    public List<Equipment> getAllEquipment() {
        List<Equipment> equipmentList = new ArrayList<>();
        String query = "SELECT * FROM equipment";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                equipmentList.add(mapResultSetToEquipment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipmentList;
    }
    
    public boolean addEquipment(Equipment equipment) {
        String query = "INSERT INTO equipment (equipment_name, serial_number, category_id, technical_specifications, storage_location, purchase_cost, purchase_date, equipment_status, assigned_to) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            setEquipmentParams(pstmt, equipment);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateEquipment(Equipment equipment) {
        String query = "UPDATE equipment SET equipment_name = ?, serial_number = ?, category_id = ?, technical_specifications = ?, storage_location = ?, purchase_cost = ?, purchase_date = ?, equipment_status = ?, assigned_to = ? WHERE equipment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            setEquipmentParams(pstmt, equipment);
            pstmt.setInt(10, equipment.getEquipmentId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public Equipment getEquipmentById(int id) {
        String query = "SELECT * FROM equipment WHERE equipment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEquipment(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void setEquipmentParams(PreparedStatement pstmt, Equipment equipment) throws SQLException {
        pstmt.setString(1, equipment.getEquipmentName());
        pstmt.setString(2, equipment.getSerialNumber());
        pstmt.setInt(3, equipment.getCategoryId());
        pstmt.setString(4, equipment.getTechnicalSpecifications());
        pstmt.setString(5, equipment.getStorageLocation());
        pstmt.setDouble(6, equipment.getPurchaseCost());
        pstmt.setString(7, equipment.getPurchaseDate());
        pstmt.setString(8, equipment.getEquipmentStatus());
        if (equipment.getAssignedTo() != null && !equipment.getAssignedTo().trim().isEmpty()) {
            pstmt.setString(9, equipment.getAssignedTo());
        } else {
            pstmt.setNull(9, Types.VARCHAR);
        }
    }
    
    private Equipment mapResultSetToEquipment(ResultSet rs) throws SQLException {
        Equipment e = new Equipment();
        e.setEquipmentId(rs.getInt("equipment_id"));
        e.setEquipmentName(rs.getString("equipment_name"));
        e.setSerialNumber(rs.getString("serial_number"));
        e.setCategoryId(rs.getInt("category_id"));
        e.setTechnicalSpecifications(rs.getString("technical_specifications"));
        e.setStorageLocation(rs.getString("storage_location"));
        e.setPurchaseCost(rs.getDouble("purchase_cost"));
        e.setPurchaseDate(rs.getString("purchase_date"));
        e.setEquipmentStatus(rs.getString("equipment_status"));
        
        String assignedTo = rs.getString("assigned_to");
        if (!rs.wasNull()) {
            e.setAssignedTo(assignedTo);
        }
        return e;
    }
}

package tracker.dao;

import tracker.db.DatabaseManager;
import tracker.models.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    
    public List<Booking> getBookingsByBorrower(int borrowerId) {
        List<Booking> list = new ArrayList<>();
        String query = "SELECT * FROM bookings WHERE borrower_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, borrowerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String query = "SELECT * FROM bookings";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRowToBooking(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public boolean addBooking(Booking b) {
        String query = "INSERT INTO bookings (equipment_id, borrower_id, start_datetime, expected_return_datetime, purpose_description, booking_status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, b.getEquipmentId());
            pstmt.setInt(2, b.getBorrowerId());
            pstmt.setString(3, b.getStartDatetime());
            pstmt.setString(4, b.getExpectedReturnDatetime());
            pstmt.setString(5, b.getPurposeDescription());
            pstmt.setString(6, b.getBookingStatus() != null ? b.getBookingStatus() : "PENDING");
            
            boolean ok = pstmt.executeUpdate() > 0;
            if (ok && "APPROVED".equals(b.getBookingStatus())) {
                updateEquipmentStatus(b.getEquipmentId(), "BORROWED");
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateBookingStatus(int bookingId, String status, Integer adminId, String rejectionReason) {
        String query = "UPDATE bookings SET booking_status = ?, admin_id = ?, rejection_reason = ? WHERE booking_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            if (adminId != null) pstmt.setInt(2, adminId);
            else pstmt.setNull(2, Types.INTEGER);
            pstmt.setString(3, rejectionReason);
            pstmt.setInt(4, bookingId);
            
            boolean ok = pstmt.executeUpdate() > 0;
            if (ok && "APPROVED".equals(status)) {
                int equipId = getEquipmentIdByBooking(bookingId);
                if (equipId > 0) {
                    updateEquipmentStatus(equipId, "BORROWED");
                }
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean recordReturn(int bookingId, String actualReturnDatetime, String returnedCondition) {
        String query = "UPDATE bookings SET booking_status = 'RETURNED', actual_return_datetime = ?, returned_condition = ? WHERE booking_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, actualReturnDatetime);
            pstmt.setString(2, returnedCondition);
            pstmt.setInt(3, bookingId);
            
            boolean ok = pstmt.executeUpdate() > 0;
            if (ok) {
                int equipId = getEquipmentIdByBooking(bookingId);
                if (equipId > 0) {
                    updateEquipmentStatus(equipId, "AVAILABLE");
                }
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getEquipmentIdByBooking(int bookingId) {
        String query = "SELECT equipment_id FROM bookings WHERE booking_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("equipment_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean updateEquipmentStatus(int equipmentId, String status) {
        String query = "UPDATE equipment SET equipment_status = ? WHERE equipment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, equipmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setEquipmentId(rs.getInt("equipment_id"));
        b.setBorrowerId(rs.getInt("borrower_id"));
        
        int adminId = rs.getInt("admin_id");
        if (!rs.wasNull()) b.setAdminId(adminId);
        
        b.setStartDatetime(rs.getString("start_datetime"));
        b.setExpectedReturnDatetime(rs.getString("expected_return_datetime"));
        b.setActualReturnDatetime(rs.getString("actual_return_datetime"));
        b.setPurposeDescription(rs.getString("purpose_description"));
        b.setBookingStatus(rs.getString("booking_status"));
        b.setApprovalStatus(rs.getString("approval_status"));
        b.setRejectionReason(rs.getString("rejection_reason"));
        b.setReturnedCondition(rs.getString("returned_condition"));
        b.setBorrowingPrice(rs.getDouble("borrowing_price"));
        return b;
    }
}

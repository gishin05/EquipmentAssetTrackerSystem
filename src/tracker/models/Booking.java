package tracker.models;

public class Booking {
    private int bookingId;
    private int equipmentId;
    private int borrowerId;
    private Integer adminId;
    private String startDatetime;
    private String expectedReturnDatetime;
    private String actualReturnDatetime;
    private String purposeDescription;
    private String bookingStatus; // PENDING, APPROVED, REJECTED, RETURNED
    private String approvalStatus;
    private String rejectionReason;
    private String returnedCondition;
    private double borrowingPrice;

    public Booking() {}

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getEquipmentId() { return equipmentId; }
    public void setEquipmentId(int equipmentId) { this.equipmentId = equipmentId; }

    public int getBorrowerId() { return borrowerId; }
    public void setBorrowerId(int borrowerId) { this.borrowerId = borrowerId; }

    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public String getStartDatetime() { return startDatetime; }
    public void setStartDatetime(String startDatetime) { this.startDatetime = startDatetime; }

    public String getExpectedReturnDatetime() { return expectedReturnDatetime; }
    public void setExpectedReturnDatetime(String expectedReturnDatetime) { this.expectedReturnDatetime = expectedReturnDatetime; }

    public String getActualReturnDatetime() { return actualReturnDatetime; }
    public void setActualReturnDatetime(String actualReturnDatetime) { this.actualReturnDatetime = actualReturnDatetime; }

    public String getPurposeDescription() { return purposeDescription; }
    public void setPurposeDescription(String purposeDescription) { this.purposeDescription = purposeDescription; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getReturnedCondition() { return returnedCondition; }
    public void setReturnedCondition(String returnedCondition) { this.returnedCondition = returnedCondition; }

    public double getBorrowingPrice() { return borrowingPrice; }
    public void setBorrowingPrice(double borrowingPrice) { this.borrowingPrice = borrowingPrice; }
}

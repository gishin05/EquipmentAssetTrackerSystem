package tracker.models;

public class MaintenanceLog {
    private int logId;
    private int equipmentId;
    private String defectDescription;
    private double partsCost;
    private String technicianDetails;
    private String startDate;
    private String completionDate;
    private String repairStatus; // IN_PROGRESS, COMPLETED

    public MaintenanceLog() {}

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getEquipmentId() { return equipmentId; }
    public void setEquipmentId(int equipmentId) { this.equipmentId = equipmentId; }

    public String getDefectDescription() { return defectDescription; }
    public void setDefectDescription(String defectDescription) { this.defectDescription = defectDescription; }

    public double getPartsCost() { return partsCost; }
    public void setPartsCost(double partsCost) { this.partsCost = partsCost; }

    public String getTechnicianDetails() { return technicianDetails; }
    public void setTechnicianDetails(String technicianDetails) { this.technicianDetails = technicianDetails; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getCompletionDate() { return completionDate; }
    public void setCompletionDate(String completionDate) { this.completionDate = completionDate; }

    public String getRepairStatus() { return repairStatus; }
    public void setRepairStatus(String repairStatus) { this.repairStatus = repairStatus; }
}

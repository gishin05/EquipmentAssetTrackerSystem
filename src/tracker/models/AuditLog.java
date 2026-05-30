package tracker.models;

public class AuditLog {
    private int auditId;
    private String actionType; // INSERT, UPDATE, DELETE
    private String affectedTable;
    private int recordId;
    private String actionTimestamp;
    private int userId;

    public AuditLog() {}

    public int getAuditId() { return auditId; }
    public void setAuditId(int auditId) { this.auditId = auditId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getAffectedTable() { return affectedTable; }
    public void setAffectedTable(String affectedTable) { this.affectedTable = affectedTable; }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public String getActionTimestamp() { return actionTimestamp; }
    public void setActionTimestamp(String actionTimestamp) { this.actionTimestamp = actionTimestamp; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}

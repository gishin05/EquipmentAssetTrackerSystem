package tracker.models;

/**
 * Represents a single field-level change record.
 * Tracks what was changed, what it was edited into, when, and by whom.
 */
public class ChangeLog {
    private int changeId;
    private String tableName;      // "Inventory" or "Booking"
    private int recordId;          // Equipment ID or Booking ID
    private String recordName;     // Equipment serial/name or Booking identifier
    private String fieldName;      // Which field was changed
    private String oldValue;       // Previous value
    private String newValue;       // New value (what it was edited into)
    private String changeTimestamp; // When the change happened
    private int userId;            // Who made the change
    private String username;       // Resolved username (not stored in DB, populated on query)

    public ChangeLog() {}

    public int getChangeId() { return changeId; }
    public void setChangeId(int changeId) { this.changeId = changeId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public String getRecordName() { return recordName; }
    public void setRecordName(String recordName) { this.recordName = recordName; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getChangeTimestamp() { return changeTimestamp; }
    public void setChangeTimestamp(String changeTimestamp) { this.changeTimestamp = changeTimestamp; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}

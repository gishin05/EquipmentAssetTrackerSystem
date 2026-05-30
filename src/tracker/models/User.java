package tracker.models;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String userRole; // ADMIN, BORROWER

    public User() {}

    public User(int userId, String username, String passwordHash, String userRole) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.userRole = userRole;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    
    @Override
    public String toString() {
        return username;
    }
}

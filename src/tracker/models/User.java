package tracker.models;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String userRole; // ADMIN, BORROWER
    private String email;
    private String fullName;
    private String themePreference; // "LIGHT" or "DARK"

    public User() {}

    public User(int userId, String username, String passwordHash, String userRole) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.userRole = userRole;
    }

    public User(int userId, String username, String passwordHash, String userRole, String email, String fullName, String themePreference) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.userRole = userRole;
        this.email = email;
        this.fullName = fullName;
        this.themePreference = themePreference;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getThemePreference() { return themePreference; }
    public void setThemePreference(String themePreference) { this.themePreference = themePreference; }
    
    @Override
    public String toString() {
        return username;
    }
}

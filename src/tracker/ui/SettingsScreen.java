package tracker.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tracker.Main;
import tracker.dao.UserDAO;
import tracker.models.User;
import tracker.util.SecurityUtil;

public class SettingsScreen {

    private Main mainApp;
    private User currentUser;
    private VBox view;
    private UserDAO userDAO;
    private Runnable onThemeChanged;

    public SettingsScreen(Main mainApp, User currentUser, Runnable onThemeChanged) {
        this.mainApp = mainApp;
        this.currentUser = currentUser;
        this.onThemeChanged = onThemeChanged;
        this.userDAO = new UserDAO();
        buildView();
    }

    public VBox getView() {
        return view;
    }

    private void buildView() {
        view = new VBox(20);
        view.setAlignment(Pos.TOP_LEFT);
        view.setPadding(new Insets(20, 0, 0, 0));

        Label title = new Label("Manage your account settings and preferences");
        title.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 14px;");

        // Card containing the form
        VBox card = new VBox(20);
        card.setMaxWidth(700);
        card.getStyleClass().addAll("card-panel");
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: -bg-card; -fx-border-color: -border-medium; -fx-border-radius: 12; -fx-background-radius: 12;");

        // Profile Settings
        Label profileLabel = new Label("Profile Information");
        profileLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -text-primary;");

        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(16);
        profileGrid.setVgap(14);

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        if (currentUser.getFullName() != null) fullNameField.setText(currentUser.getFullName());

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        if (currentUser.getEmail() != null) emailField.setText(currentUser.getEmail());

        ComboBox<String> themeBox = new ComboBox<>();
        themeBox.getItems().addAll("LIGHT", "DARK");
        String currentTheme = currentUser.getThemePreference() != null ? currentUser.getThemePreference() : "DARK";
        themeBox.setValue(currentTheme);

        Label l1 = new Label("Full Name"); l1.getStyleClass().add("form-label");
        Label l2 = new Label("Email");     l2.getStyleClass().add("form-label");
        Label l3 = new Label("Theme");     l3.getStyleClass().add("form-label");

        profileGrid.addRow(0, l1, fullNameField);
        profileGrid.addRow(1, l2, emailField);
        profileGrid.addRow(2, l3, themeBox);

        Separator sep = new Separator();

        // Password Settings
        Label pwdLabel = new Label("Change Password");
        pwdLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -text-primary;");
        Label pwdSubLabel = new Label("Leave blank to keep your current password.");
        pwdSubLabel.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 12px;");

        GridPane pwdGrid = new GridPane();
        pwdGrid.setHgap(16);
        pwdGrid.setVgap(14);

        PasswordField currentPwdField = new PasswordField();
        currentPwdField.setPromptText("Current Password");
        PasswordField newPwdField = new PasswordField();
        newPwdField.setPromptText("New Password");
        PasswordField confirmPwdField = new PasswordField();
        confirmPwdField.setPromptText("Confirm New Password");

        Label pl1 = new Label("Current"); pl1.getStyleClass().add("form-label");
        Label pl2 = new Label("New");     pl2.getStyleClass().add("form-label");
        Label pl3 = new Label("Confirm"); pl3.getStyleClass().add("form-label");

        pwdGrid.addRow(0, pl1, currentPwdField);
        pwdGrid.addRow(1, pl2, newPwdField);
        pwdGrid.addRow(2, pl3, confirmPwdField);

        // Save Button
        Button saveBtn = new Button("Save Settings");
        saveBtn.getStyleClass().addAll("button", "btn-primary");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            // Validate password change if any field is filled
            boolean updatePassword = false;
            String currentPwd = currentPwdField.getText();
            String newPwd = newPwdField.getText();
            String confirmPwd = confirmPwdField.getText();

            if (!currentPwd.isEmpty() || !newPwd.isEmpty() || !confirmPwd.isEmpty()) {
                String hashedCurrent = SecurityUtil.hashPassword(currentPwd);
                if (!currentUser.getPasswordHash().equals(hashedCurrent)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect!");
                    return;
                }
                if (newPwd.isEmpty() || !newPwd.equals(confirmPwd)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match or are empty!");
                    return;
                }
                updatePassword = true;
            }

            // Update Profile
            currentUser.setFullName(fullNameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setThemePreference(themeBox.getValue());
            
            boolean updatedProfile = userDAO.updateUserSettings(currentUser);
            boolean updatedPwd = true;
            if (updatePassword) {
                updatedPwd = userDAO.updatePassword(currentUser.getUserId(), SecurityUtil.hashPassword(newPwd));
            }

            if (updatedProfile && updatedPwd) {
                
                // Call theme changed callback
                if (onThemeChanged != null) {
                    onThemeChanged.run();
                }
                
                // Clear password fields
                currentPwdField.clear();
                newPwdField.clear();
                confirmPwdField.clear();
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Settings updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update settings in database.");
            }
        });

        card.getChildren().addAll(
            profileLabel, profileGrid,
            sep,
            pwdLabel, pwdSubLabel, pwdGrid,
            saveBtn
        );

        view.getChildren().addAll(title, card);
    }



    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(mainApp.getPrimaryStage());
        alert.showAndWait();
    }
}

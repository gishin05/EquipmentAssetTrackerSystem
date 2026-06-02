package tracker.ui;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import tracker.Main;
import tracker.dao.UserDAO;
import tracker.models.User;
import tracker.util.SecurityUtil;

public class LoginScreen {
    private Main mainApp;
    private StackPane view;
    private UserDAO userDAO;

    /**
     * Create the login screen.
     */
    public LoginScreen(Main mainApp) {
        this.mainApp = mainApp;
        this.userDAO = new UserDAO();
        buildView();
    }

    public StackPane getView() {
        return view;
    }

    private void buildView() {
        view = new StackPane();
        view.getStyleClass().add("login-root");
        view.setAlignment(Pos.CENTER);

        // ── Login Card ──
        VBox loginCard = new VBox(8);
        loginCard.getStyleClass().add("login-card");
        loginCard.setAlignment(Pos.CENTER_LEFT);
        loginCard.setMaxWidth(440);
        loginCard.setMaxHeight(Region.USE_PREF_SIZE);

        // Subtitle
        Label subtitle = new Label("Sign in to Equipment Asset Tracker");
        subtitle.getStyleClass().add("login-subtitle");

        Region spacer1 = new Region();
        spacer1.setMinHeight(20);

        // Username field
        Label usernameLabel = new Label("USERNAME");
        usernameLabel.getStyleClass().add("login-field-label");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setId("login-username");

        Region spacer2 = new Region();
        spacer2.setMinHeight(6);

        // Password field
        Label passwordLabel = new Label("PASSWORD");
        passwordLabel.getStyleClass().add("login-field-label");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setId("login-password");

        // Error label (hidden by default)
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("login-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Region spacer3 = new Region();
        spacer3.setMinHeight(12);

        // Login button
        Button loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().addAll("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setId("login-button");

        // Footer text
        Region spacer4 = new Region();
        spacer4.setMinHeight(8);

        Label footerText = new Label("Default admin: Admin / Admin");
        footerText.getStyleClass().add("login-subtitle");
        footerText.setStyle("-fx-font-size: 11px;");
        footerText.setAlignment(Pos.CENTER);

        loginCard.getChildren().addAll(
            subtitle,
            spacer1,
            usernameLabel, usernameField,
            spacer2,
            passwordLabel, passwordField,
            errorLabel,
            spacer3,
            loginBtn,
            spacer4,
            footerText
        );

        // Center the card
        HBox centerBox = new HBox(loginCard);
        centerBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(loginCard, Priority.NEVER);

        view.getChildren().add(centerBox);

        // ── Login Action ──
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showError(errorLabel, "Please enter both username and password.");
                return;
            }

            String hash = SecurityUtil.hashPassword(password);
            User user = userDAO.authenticate(username, hash);

            if (user != null) {
                if ("ADMIN".equals(user.getUserRole())) {
                    mainApp.showAdminDashboard(user);
                } else {
                    mainApp.showBorrowerPortal(user);
                }
            } else {
                showError(errorLabel, "Invalid credentials. Please try again.");
                shakeNode(loginCard);
            }
        });

        // Allow Enter key to submit
        passwordField.setOnAction(e -> loginBtn.fire());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        // ── Entrance Animation ──
        loginCard.setOpacity(0);
        loginCard.setTranslateY(30);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), loginCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), loginCard);
        slideUp.setFromY(30);
        slideUp.setToY(0);

        fadeIn.play();
        slideUp.play();
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.millis(300), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void shakeNode(VBox node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> node.setTranslateX(0));
        shake.play();
    }
}

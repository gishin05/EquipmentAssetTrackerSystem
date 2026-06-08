package tracker.ui;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
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
        // Core view root layer setup
        view = new StackPane();
        view.getStyleClass().add("login-root");
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);

        // ── Split Pane Horizontal Master Layout ──
        HBox splitContainer = new HBox();
        splitContainer.setMaxWidth(Double.MAX_VALUE);
        splitContainer.setMaxHeight(Double.MAX_VALUE);

        // ═══════════════════════════════════════════════════════════
        // LEFT SPLIT-PANE: Structural Sync with Technical Specs Document
        // ═══════════════════════════════════════════════════════════
        VBox leftPane = new VBox(38); 
        leftPane.getStyleClass().add("login-left-pane");
        leftPane.setPadding(new Insets(60, 50, 60, 65));
        leftPane.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        leftPane.setMaxWidth(Double.MAX_VALUE);

        // Header Title / Subsystem Brand Block
        VBox brandBlock = new VBox(4);
        Label brandTitle = new Label("Obsidian Pro");
        brandTitle.setStyle("-fx-font-family: 'Inter'; -fx-font-weight: 900; -fx-font-size: 22px; -fx-text-fill: #ffffff; -fx-letter-spacing: -0.5px;");
        Label brandSubtitle = new Label("Equipment Asset Tracker System");
        brandSubtitle.setStyle("-fx-font-family: 'Inter'; -fx-font-weight: 500; -fx-font-size: 12px; -fx-text-fill: #7b94ff;");
        brandBlock.getChildren().addAll(brandTitle, brandSubtitle);

        // Mini Platform Status Tag
        Label tagLabel = new Label("• OFFLINE TERMINAL CORE");
        tagLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-weight: 800; -fx-font-size: 10px; -fx-text-fill: #7b94ff; -fx-background-color: rgba(79, 110, 247, 0.16); -fx-padding: 5 10 5 10; -fx-background-radius: 20;");

        // System Objectives Typography Block
        VBox marketingText = new VBox(12);
        Label mainHeading = new Label("Track Your\nHardware Smarter,\nNot Harder.");
        mainHeading.setStyle("-fx-font-family: 'Inter'; -fx-font-weight: 900; -fx-font-size: 38px; -fx-text-fill: #ffffff; -fx-line-spacing: -4px;");
        Label miniDescription = new Label("A secure desktop application to comprehensively manage organizational hardware via a self-contained local SQLite architecture.");
        miniDescription.setStyle("-fx-font-family: 'Inter'; -fx-text-fill: rgba(240, 240, 250, 0.7); -fx-font-size: 13.5px; -fx-max-width: 440px; -fx-line-spacing: 2px;");
        miniDescription.setWrapText(true);
        marketingText.getChildren().addAll(mainHeading, miniDescription);

        // Functional Feature Verification List Map (Pulled from Document Specifications)
        VBox featuresList = new VBox(16);
        featuresList.getChildren().addAll(
            createFeatureItem("📦 Asset Inventory Manager", "Real-time visibility into local hardware availability and system user assignments."),
            createFeatureItem("⚡ Loan Lifecycle Workflow", "Automated deployment verification checking to strictly enforce check-out and check-in steps."),
            createFeatureItem("🛡️ Automated Conflict Prevention", "Built-in database validation blocks double-bookings and chronological schedule overlaps."),
            createFeatureItem("🛠️ Repair Registry Engine", "Connects inventory metrics directly with ongoing defect logging and fault isolation maintenance dispatching.")
        );

        Region leftSpacer = new Region();
        VBox.setVgrow(leftSpacer, Priority.ALWAYS);
        
        Label copyrightLabel = new Label("© 2026 Equipment Asset Tracker — Zero-Configuration Deployment Runtime");
        copyrightLabel.setStyle("-fx-font-family: 'Inter'; -fx-text-fill: rgba(240, 240, 250, 0.35); -fx-font-size: 11px;");

        leftPane.getChildren().addAll(brandBlock, tagLabel, marketingText, featuresList, leftSpacer, copyrightLabel);

        // ═══════════════════════════════════════════════════════════
        // RIGHT SPLIT-PANE: Clean Form Authentication Workspace
        // ═══════════════════════════════════════════════════════════
        StackPane rightPane = new StackPane();
        rightPane.getStyleClass().add("login-right-pane");
        HBox.setHgrow(rightPane, Priority.ALWAYS);
        rightPane.setMaxWidth(Double.MAX_VALUE);
        rightPane.setAlignment(Pos.CENTER);

        // Form Content Context Card Container
        VBox loginCard = new VBox(24); 
        loginCard.getStyleClass().add("login-card");
        loginCard.setAlignment(Pos.CENTER_LEFT);
        loginCard.setMinWidth(380);
        loginCard.setMaxWidth(420);

        // Top Form Welcoming Segment
        VBox headerBlock = new VBox(6);
        Label titleLabel = new Label("Welcome back, Admin!");
        titleLabel.getStyleClass().add("login-title");
        Label subtitle = new Label("Sign in to your account to access your system control dashboard.");
        subtitle.getStyleClass().add("login-subtitle");
        subtitle.setWrapText(true);
        headerBlock.getChildren().addAll(titleLabel, subtitle);

        // Username layout segment
        VBox usernameBlock = new VBox(8);
        Label usernameLabel = new Label("USERNAME / ASSET ID");
        usernameLabel.getStyleClass().add("login-field-label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username or ID...");
        usernameField.setId("login-username");
        usernameBlock.getChildren().addAll(usernameLabel, usernameField);

        // Password layout segment
        VBox passwordBlock = new VBox(8);
        Label passwordLabel = new Label("PASSWORD");
        passwordLabel.getStyleClass().add("login-field-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("••••••••••••");
        passwordField.setId("login-password");
        passwordBlock.getChildren().addAll(passwordLabel, passwordField);

        // Operational Error Label (Handled via CSS transitions)
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("login-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Extended interface actions contextual layer (Remember Me / Forgot Access)
        HBox actionsRow = new HBox();
        actionsRow.setAlignment(Pos.CENTER_LEFT);
        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setStyle("-fx-text-fill: #7e7ea8; -fx-font-size: 12.5px; -fx-cursor: hand;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Hyperlink forgotPassword = new Hyperlink("Forgot password?");
        actionsRow.getChildren().addAll(rememberMe, spacer, forgotPassword);

        // Primary Action Submissions button
        Button loginBtn = new Button("Sign In to Secure Terminal");
        loginBtn.getStyleClass().addAll("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setId("login-button");

        // Footer block segment
        Label footerText = new Label("Default admin: Admin / Admin");
        footerText.getStyleClass().add("login-subtitle");
        footerText.setStyle("-fx-font-size: 11px;");
        footerText.setAlignment(Pos.CENTER);

        // Bind interactive right elements into card pane
        loginCard.getChildren().addAll(
            headerBlock,
            usernameBlock,
            passwordBlock,
            errorLabel,
            actionsRow,
            loginBtn,
            footerText
        );

        // Final layout staging assembly links
        rightPane.getChildren().add(loginCard);
        splitContainer.getChildren().addAll(leftPane, rightPane);
        view.getChildren().add(splitContainer);

        // ── Database Access / Login Action Control Map ──
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

        // Event listener hooks for field submission shortcuts
        passwordField.setOnAction(e -> loginBtn.fire());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        // ── Entrance Animation Transition Matrices ──
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

    /**
     * Generates clean typographic layout entries for the platform feature list checklist.
     */
    private HBox createFeatureItem(String title, String desc) {
        VBox text = new VBox(2);
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #f0f0fa; -fx-font-family: 'Inter'; -fx-font-weight: 700; -fx-font-size: 13.5px;");
        Label d = new Label(desc);
        d.setStyle("-fx-text-fill: #7e7ea8; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-max-width: 440px;");
        d.setWrapText(true);
        text.getChildren().addAll(t, d);
        
        HBox row = new HBox(12, text);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
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
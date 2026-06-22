package tracker.ui;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import tracker.Main;
import tracker.dao.UserDAO;
import tracker.models.User;
import tracker.util.SecurityUtil;

public class LoginScreen {
    private Main mainApp;
    private StackPane view;
    private UserDAO userDAO;

    public LoginScreen(Main mainApp) {
        this.mainApp = mainApp;
        this.userDAO = new UserDAO();
        buildView();
    }

    public StackPane getView() {
        return view;
    }

    private void buildView() {
        // ── Root: soft lavender canvas background (matches --body-bg: #f4f3fb) ──
        view = new StackPane();
        view.getStyleClass().add("login-root");
        view.setAlignment(Pos.CENTER);

        // ══════════════════════════════════════════════════════════════
        // THE CENTERED SPLIT CARD  (900px wide, 560px tall, radius 20)
        // ══════════════════════════════════════════════════════════════
        HBox splitCard = new HBox();
        splitCard.getStyleClass().add("login-split-card");
        // Increase overall card width so the left branding panel has more room
        splitCard.setPrefWidth(980);
        splitCard.setMaxWidth(980);
        splitCard.setMinWidth(980);
        splitCard.setPrefHeight(560);
        splitCard.setMaxHeight(560);
        splitCard.setMinHeight(560);

        // Clip the card to rounded corners
        // Match the clip size to the new card width
        Rectangle clip = new Rectangle(980, 560);
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        splitCard.setClip(clip);

        // ═══════════════════════════════════════
        // LEFT HALF — Purple gradient branding
        // ═══════════════════════════════════════
        VBox leftPane = new VBox(0);
        leftPane.getStyleClass().add("login-left-panel");
        leftPane.setPadding(new Insets(48, 44, 44, 44));
        leftPane.setAlignment(Pos.TOP_LEFT);
        // Give the left branding panel more horizontal space so feature descriptions fit
        leftPane.setPrefWidth(520);
        leftPane.setMinWidth(520);
        leftPane.setMaxWidth(520);

        // ── Logo block ──
        HBox logoRow = new HBox(12);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        StackPane logoIcon = new StackPane();
        logoIcon.getStyleClass().add("login-logo-icon");
        logoIcon.setMinSize(40, 40);
        logoIcon.setMaxSize(40, 40);
        Label logoIconLabel = new Label("⬡");
        logoIconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        logoIcon.getChildren().add(logoIconLabel);

        VBox logoText = new VBox(2);
        Label logoName = new Label("Obsidian Pro");
        logoName.getStyleClass().add("login-logo-name");
        Label logoSub = new Label("Equipment Asset Tracker");
        logoSub.getStyleClass().add("login-logo-sub");
        logoText.getChildren().addAll(logoName, logoSub);
        logoRow.getChildren().addAll(logoIcon, logoText);

        // ── Headline ──
        Label headline = new Label("Track Your\nHardware Smarter");
        headline.getStyleClass().add("login-headline");
        // Allow the headline to wrap instead of being clipped and expand vertically
        headline.setWrapText(true);
        headline.setMaxWidth(440);
        headline.setMinHeight(Region.USE_PREF_SIZE);
        VBox.setMargin(headline, new Insets(24, 0, 24, 0));

        // ── Feature list ──
        VBox featuresList = new VBox(18);
        featuresList.getChildren().addAll(
            createFeatureItem("📦", "Asset Inventory Manager",
                "Real-time visibility into hardware availability and user assignments."),
            createFeatureItem("⚡", "Loan Lifecycle Workflow",
                "Automated deployment verification for check-out and check-in steps."),
            createFeatureItem("🛡️", "Conflict Prevention",
                "Built-in validation blocks double-bookings and schedule overlaps."),
            createFeatureItem("🛠️", "Repair Registry Engine",
                "Connects inventory metrics with defect logging and maintenance.")
        );

        Region leftSpacer = new Region();
        VBox.setVgrow(leftSpacer, Priority.ALWAYS);

        Label copyright = new Label("© 2026 Equipment Asset Tracker System");
        copyright.getStyleClass().add("login-copyright");

        leftPane.getChildren().addAll(logoRow, headline, featuresList, leftSpacer, copyright);

        // ═══════════════════════════════════════
        // RIGHT HALF — Clean white login form
        // ═══════════════════════════════════════
        VBox rightPane = new VBox(0);
        rightPane.getStyleClass().add("login-right-panel");
        rightPane.setPadding(new Insets(48, 48, 48, 48));
        rightPane.setAlignment(Pos.CENTER_LEFT);
        // Keep the right pane at 450 so the overall card aligns with the new width
        rightPane.setPrefWidth(450);
        rightPane.setMinWidth(450);
        rightPane.setMaxWidth(450);

        // Greeting
        Label greeting = new Label("Welcome back, Admin!");
        greeting.getStyleClass().add("login-greeting");

        Label loginSub = new Label("Sign in to access your system control dashboard.");
        loginSub.getStyleClass().add("login-sub");
        loginSub.setWrapText(true);
        VBox.setMargin(loginSub, new Insets(4, 0, 22, 0));

        // Username field
        Label usernameLabel = new Label("USERNAME / ASSET ID");
        usernameLabel.getStyleClass().add("login-field-label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username or ID...");
        usernameField.getStyleClass().add("login-input");
        VBox usernameBlock = new VBox(6, usernameLabel, usernameField);
        VBox.setMargin(usernameBlock, new Insets(0, 0, 14, 0));

        // Password field
        Label passwordLabel = new Label("PASSWORD");
        passwordLabel.getStyleClass().add("login-field-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("••••••••••••");
        passwordField.getStyleClass().add("login-input");
        VBox passwordBlock = new VBox(6, passwordLabel, passwordField);
        VBox.setMargin(passwordBlock, new Insets(0, 0, 14, 0));

        // Remember me + Forgot password row
        HBox optionsRow = new HBox();
        optionsRow.setAlignment(Pos.CENTER_LEFT);
        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.getStyleClass().add("login-check");
        Region rowSpacer = new Region();
        HBox.setHgrow(rowSpacer, Priority.ALWAYS);
        Hyperlink forgotLink = new Hyperlink("Forgot password?");
        forgotLink.getStyleClass().add("login-forgot");
        optionsRow.getChildren().addAll(rememberMe, rowSpacer, forgotLink);
        VBox.setMargin(optionsRow, new Insets(0, 0, 18, 0));

        // Error label
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("login-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setWrapText(true);
        VBox.setMargin(errorLabel, new Insets(0, 0, 12, 0));

        // Sign in button
        Button loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("login-btn");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        // Default credentials hint
        Label defaultHint = new Label("Default admin: Admin / Admin");
        defaultHint.getStyleClass().add("login-default-hint");
        defaultHint.setMaxWidth(Double.MAX_VALUE);
        defaultHint.setAlignment(Pos.CENTER);
        VBox.setMargin(defaultHint, new Insets(14, 0, 0, 0));

        rightPane.getChildren().addAll(
            greeting, loginSub,
            usernameBlock, passwordBlock,
            optionsRow, errorLabel,
            loginBtn, defaultHint
        );

        // ── Assemble card ──
        splitCard.getChildren().addAll(leftPane, rightPane);

        // ── Card drop shadow wrapper ──
        StackPane cardWrapper = new StackPane(splitCard);
        cardWrapper.getStyleClass().add("login-card-shadow");
        cardWrapper.setMaxWidth(920);

        view.getChildren().add(cardWrapper);

        // ── Login logic ──
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
                if ("DARK".equalsIgnoreCase(user.getThemePreference())) {
                    if (!view.getScene().getRoot().getStyleClass().contains("dark-theme")) {
                        view.getScene().getRoot().getStyleClass().add("dark-theme");
                    }
                } else {
                    view.getScene().getRoot().getStyleClass().remove("dark-theme");
                }

                if ("ADMIN".equals(user.getUserRole())) {
                    mainApp.showAdminDashboard(user);
                } else {
                    mainApp.showBorrowerPortal(user);
                }
            } else {
                showError(errorLabel, "Invalid credentials. Please try again.");
                shakeNode(splitCard);
            }
        });

        passwordField.setOnAction(e -> loginBtn.fire());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        // ── Entrance animation ──
        splitCard.setOpacity(0);
        splitCard.setTranslateY(24);
        FadeTransition fade = new FadeTransition(Duration.millis(500), splitCard);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(500), splitCard);
        slide.setFromY(24);
        slide.setToY(0);
        fade.play();
        slide.play();
    }

    private HBox createFeatureItem(String emoji, String title, String desc) {
        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("login-feature-icon");
        iconBox.setMinSize(32, 32);
        iconBox.setMaxSize(32, 32);
        Label iconLabel = new Label(emoji);
        iconLabel.setStyle("-fx-font-size: 13px;");
        iconBox.getChildren().add(iconLabel);

        VBox text = new VBox(2);
        Label t = new Label(title);
        t.getStyleClass().add("login-feature-title");
        Label d = new Label(desc);
        d.getStyleClass().add("login-feature-desc");
        // Ensure descriptions wrap and have sufficient width to avoid truncation
        d.setWrapText(true);
        d.setMaxWidth(320);
        d.setPrefWidth(320);
        // Let the label expand vertically to fit its content
        d.setMinHeight(Region.USE_PREF_SIZE);
        // Allow the text column to grow in the HBox so wrapping behaves correctly
        HBox.setHgrow(text, Priority.ALWAYS);
        text.getChildren().addAll(t, d);

        HBox row = new HBox(12, iconBox, text);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        FadeTransition fade = new FadeTransition(Duration.millis(250), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void shakeNode(HBox node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> node.setTranslateX(0));
        shake.play();
    }
}
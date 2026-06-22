package tracker.ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.stage.FileChooser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import tracker.Main;
import tracker.dao.*;
import tracker.models.*;

import java.util.List;

public class AdminDashboard {
    private Main mainApp;
    private User currentUser;
    private StackPane rootView;
    private BorderPane mainView;
    private VBox sidePanel;
    private boolean isLightMode = false;
    private StackPane contentArea;
    private VBox navBtnContainer;
    private Button activeNavBtn;
    private Label headerTitle;

    // DAOs
    private EquipmentDAO equipmentDAO = new EquipmentDAO();
    private BookingDAO bookingDAO = new BookingDAO();
    private MaintenanceDAO maintenanceDAO = new MaintenanceDAO();
    private AuditDAO auditDAO = new AuditDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private ChangeLogDAO changeLogDAO = new ChangeLogDAO();

    /**
     * Create the admin dashboard.
     */
    public AdminDashboard(Main mainApp, User user) {
        this.mainApp = mainApp;
        this.currentUser = user;
        buildView();
    }

    public javafx.scene.Parent getView() {
        return rootView;
    }
    
    private void buildView() {
        rootView = new StackPane();
        mainView = new BorderPane();
     
        // ── Vertical Sidebar Navigation ──
        VBox sidebar = buildSidebarLayout();
        mainView.setLeft(sidebar);
     
        // ── Main Content Container ──
        // Use zero vertical spacing so the header can visually flush to the separator;
        // specific vertical gaps are managed via padding/margins on the children.
        // ── Main Content Container ──
        VBox mainViewContainer = new VBox(0);
        mainViewContainer.getStyleClass().add("main-content");
     
        // Header Section (Title + Subtitle on Left, Icons on Right)
        HBox headerSection = new HBox();
        headerSection.setAlignment(Pos.CENTER_LEFT);
        headerSection.getStyleClass().add("header-bar");
        headerSection.setPadding(new Insets(18, 32, 18, 32));
        
        VBox titleBox = new VBox(4);
        headerTitle = new Label("Dashboard");
        headerTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: -text-primary;");
        
        Label headerSubtitle = new Label(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        headerSubtitle.setStyle("-fx-font-family: 'Poppins'; -fx-text-fill: -text-secondary; -fx-font-size: 12px;");
        titleBox.getChildren().addAll(headerTitle, headerSubtitle);
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        HBox rightIcons = new HBox(15);
        rightIcons.setAlignment(Pos.CENTER);
        
        Label bellIcon = new Label("\uD83D\uDD14");
        bellIcon.setStyle("-fx-background-color: #f4f3fb; -fx-text-fill: #9d9bb8; -fx-font-size: 14px; -fx-padding: 8; -fx-background-radius: 20; -fx-cursor: hand;");
        
        bellIcon.setOnMouseClicked(e -> {
            showNotificationPopup(bellIcon);
        });

        Label avatar = new Label("A");
        avatar.setStyle("-fx-background-color: #5b47e0; -fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: hand;");
        
        rightIcons.getChildren().addAll(bellIcon, avatar);
        headerSection.getChildren().addAll(titleBox, headerSpacer, rightIcons);
     
        // ── Separator line below the header ──
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #e4e7ec; -fx-padding: 0;");
     
        // ── Scrollable Content Area ──
        VBox scrollContentLayout = new VBox(0);
        scrollContentLayout.setPadding(new Insets(24, 32, 24, 32));
        scrollContentLayout.getStyleClass().add("main-content");
     
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        contentArea.setPadding(new Insets(0));
        VBox.setVgrow(contentArea, Priority.ALWAYS);
     
        scrollContentLayout.getChildren().add(contentArea);
     
        ScrollPane scrollPane = new ScrollPane(scrollContentLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        mainViewContainer.getChildren().addAll(headerSection, separator, scrollPane);
        mainView.setCenter(mainViewContainer);
     
        buildSidePanel();
        rootView.getChildren().addAll(mainView, sidePanel);
     
        showDashboard();
    }
    // ═══════════════════════════════════════════════════════════
    //  SIDEBAR NAVIGATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Builds the primary vertical sidebar navigation engine docked to the left.
     * Contains branding block, categorised nav buttons, and a bottom user utility strip.
     */
    
    private VBox buildSidebarLayout() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(245);
        sidebar.setMinWidth(245);
        sidebar.setMaxWidth(245);
        sidebar.setStyle(
            "-fx-background-color: #1a1825;" +
            "-fx-border-color: #2a2740;" +
            "-fx-border-width: 0 1 0 0;"
        );
     
        // ── BRANDING BLOCK ──────────────────────────────────────
        VBox brandingBlock = new VBox(3);
        brandingBlock.setPadding(new Insets(24, 20, 20, 20));
        brandingBlock.setStyle("-fx-border-color: #2a2740; -fx-border-width: 0 0 1 0;");
     
        Label appTitle = new Label("Obsidian Pro");
        appTitle.setStyle(
            "-fx-font-family: 'Poppins';" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: 900;" +
            "-fx-text-fill: #ffffff;"
        );
     
        Label appSubtitle = new Label("EQUIPMENT TRACKER");
        appSubtitle.setStyle(
            "-fx-font-family: 'Poppins';" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #7b94ff;" +
            "-fx-letter-spacing: 1px;"
        );
     
        brandingBlock.getChildren().addAll(appTitle, appSubtitle);
     
        // ── NAV BUTTON CONTAINER ────────────────────────────────
     // ── NAV BUTTON CONTAINER ────────────────────────────────
        navBtnContainer = new VBox(2);
        navBtnContainer.setPadding(new Insets(16, 12, 12, 12));
        VBox.setVgrow(navBtnContainer, Priority.ALWAYS);

        // MAIN SYSTEM
        navBtnContainer.getChildren().add(buildNavCategory("MAIN SYSTEM"));
        Button btnDashboard  = createNavButton("Dashboard", "🏠", () -> showDashboard());
        Button btnInventory  = createNavButton("Inventory", "📦", () -> showInventory());
        navBtnContainer.getChildren().addAll(btnDashboard, btnInventory);

        // OPERATIONS
        navBtnContainer.getChildren().add(buildNavCategory("OPERATIONS"));
        Button btnBooking     = createNavButton("Booking Requests", "📥", () -> showLoanApprovals());
        Button btnMaintenance = createNavButton("Maintenance Logs", "🛠", () -> showMaintenance());
        navBtnContainer.getChildren().addAll(btnBooking, btnMaintenance);

        // SYSTEM
        navBtnContainer.getChildren().add(buildNavCategory("SYSTEM"));
        Button btnAuditLogs   = createNavButton("Logs", "📜", () -> showAuditLogs());
        navBtnContainer.getChildren().add(btnAuditLogs);
        

        // Set default active button
        setActiveNavBtn(btnDashboard);

        // ── BOTTOM USER STRIP ───────────────────────────────────
        VBox userStrip = new VBox(6);
        userStrip.setPadding(new Insets(12, 12, 20, 12));
        userStrip.setStyle("-fx-border-color: #2a2740; -fx-border-width: 1 0 0 0;");

        Label userName = new Label(currentUser.getUsername());
        userName.setStyle(
            "-fx-font-family: 'Poppins';" +
            "-fx-text-fill: #ffffff;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;"
        );
        

        Label roleBadgeLabel = new Label("ADMIN");
        roleBadgeLabel.getStyleClass().add("header-role-badge");
     
        Button settingsBtn = new Button("\u2699 Settings");
        settingsBtn.getStyleClass().add("side-panel-btn");
        settingsBtn.setMaxWidth(Double.MAX_VALUE);
        settingsBtn.setOnAction(e -> showSettings());

        Button logoutBtn = new Button("⏻  Logout");
        logoutBtn.getStyleClass().add("side-panel-btn-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            if (rootView.getScene() != null) {
                rootView.getScene().getRoot().getStyleClass().remove("dark-theme");
            }
            mainApp.showLoginScreen();
        });
     
        userStrip.getChildren().addAll(settingsBtn, logoutBtn);
     
        // ── ASSEMBLE SIDEBAR ────────────────────────────────────
        sidebar.getChildren().addAll(brandingBlock, navBtnContainer, userStrip);
        return sidebar;
    }

    /** Returns a styled category header label for sidebar nav groupings. */
    /** Returns a styled category header label for sidebar nav groupings. */
    private Label buildNavCategory(String text) {
        Label lbl = new Label(text);
        
        // Add the clean CSS class name so it can be styled from styles.css
        lbl.getStyleClass().add("sidebar-category-header");
        
        return lbl;
    }
    

    private Button createNavButton(String text, String icon, Runnable action) {
        Button btn = new Button();
        // Build an HBox with an icon label and a text label so we can style them independently
        HBox content = new HBox(10);
        content.setAlignment(Pos.CENTER_LEFT);

        Label iconLbl = new Label(icon);
        iconLbl.getStyleClass().add("nav-icon");

        Label textLbl = new Label(text);
        textLbl.getStyleClass().add("nav-text");

        content.getChildren().addAll(iconLbl, textLbl);
        btn.setGraphic(content);

        btn.getStyleClass().add("header-nav-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);

        // Inline code fix: Forces JavaFX to completely drop its built-in blue outline engine
        // directly on the control instance so it uses only your CSS class rules
        btn.setStyle(
            "-fx-background-insets: 0;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );

        btn.setOnAction(e -> {
            setActiveNavBtn(btn);
            action.run();
        });
        return btn;
    }
    private void setActiveNavBtn(Button btn) {
        if (activeNavBtn != null) {
            activeNavBtn.getStyleClass().remove("header-nav-btn-active");
        }
        btn.getStyleClass().add("header-nav-btn-active");
        activeNavBtn = btn;
        
    }
    
    

    /**
     * Retained legacy floating side panel — repurposed as the popup user-settings
     * overlay triggered from account actions. Hidden by default.
     */
    private void buildSidePanel() {
        sidePanel = new VBox(10);
        sidePanel.getStyleClass().add("side-panel");
        sidePanel.setPrefWidth(220);
        sidePanel.setMaxWidth(220);
        sidePanel.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(sidePanel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(sidePanel, new Insets(0, 0, 20, 255));
        sidePanel.setVisible(false);
    }

    // ═══════════════════════════════════════════════════════════
    //  NOTIFICATION POPUP
    // ═══════════════════════════════════════════════════════════

    private javafx.stage.Popup activeNotificationPopup;

    private void showNotificationPopup(javafx.scene.Node anchor) {
        // Close existing popup if open
        if (activeNotificationPopup != null && activeNotificationPopup.isShowing()) {
            activeNotificationPopup.hide();
            activeNotificationPopup = null;
            return;
        }

        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.setAutoHide(true);
        activeNotificationPopup = popup;

        VBox container = new VBox(0);
        container.setMinWidth(340);
        container.setMaxWidth(340);
        container.setStyle(
            "-fx-background-color: -bg-card; -fx-background-radius: 12; " +
            "-fx-border-color: -border; -fx-border-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 16, 0, 0, 4);"
        );

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 16, 10, 16));
        Label titleLbl = new Label("Notifications");
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -text-primary;");
        header.getChildren().add(titleLbl);

        Separator sep = new Separator();

        VBox itemsBox = new VBox(0);
        itemsBox.setPadding(new Insets(6, 0, 6, 0));

        // ── Gather real notifications ──
        List<Booking> allBookings = bookingDAO.getAllBookings();
        List<Equipment> allEquipment = equipmentDAO.getAllEquipment();

        // 1. Pending bookings needing approval
        long pendingCount = allBookings.stream()
            .filter(b -> "PENDING".equals(b.getBookingStatus()))
            .count();
        if (pendingCount > 0) {
            itemsBox.getChildren().add(buildNotifItem(
                "\u23F3", "#f59e0b",
                pendingCount + " booking" + (pendingCount > 1 ? "s" : "") + " awaiting approval",
                "Review and approve pending requests"
            ));
        }

        // 2. Overdue returns (approved bookings past expected return date)
        String today = java.time.LocalDate.now().toString();
        long overdueCount = allBookings.stream()
            .filter(b -> "APPROVED".equals(b.getBookingStatus()))
            .filter(b -> b.getExpectedReturnDatetime() != null && b.getExpectedReturnDatetime().compareTo(today) < 0)
            .count();
        if (overdueCount > 0) {
            itemsBox.getChildren().add(buildNotifItem(
                "\u26A0", "#ef4444",
                overdueCount + " overdue return" + (overdueCount > 1 ? "s" : ""),
                "Equipment not returned by expected date"
            ));
        }

        // 3. Equipment in maintenance
        long maintCount = allEquipment.stream()
            .filter(eq -> "UNDER_MAINTENANCE".equals(eq.getEquipmentStatus()))
            .count();
        if (maintCount > 0) {
            itemsBox.getChildren().add(buildNotifItem(
                "\uD83D\uDD27", "#3b82f6",
                maintCount + " equipment in maintenance",
                "Items currently unavailable for booking"
            ));
        }

        // 4. Low availability warning
        long availableCount = allEquipment.stream()
            .filter(eq -> "AVAILABLE".equals(eq.getEquipmentStatus()))
            .count();
        long totalEquip = allEquipment.size();
        if (totalEquip > 0 && availableCount * 100 / totalEquip < 20) {
            itemsBox.getChildren().add(buildNotifItem(
                "\uD83D\uDCE6", "#8b5cf6",
                "Low equipment availability",
                "Only " + availableCount + " of " + totalEquip + " items available"
            ));
        }

        // If no notifications
        if (itemsBox.getChildren().isEmpty()) {
            Label emptyLbl = new Label("You're all caught up!");
            emptyLbl.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 13px; -fx-padding: 20 0;");
            emptyLbl.setMaxWidth(Double.MAX_VALUE);
            emptyLbl.setAlignment(Pos.CENTER);
            itemsBox.getChildren().add(emptyLbl);
        }

        container.getChildren().addAll(header, sep, itemsBox);
        popup.getContent().add(container);

        // Position below the bell icon, aligned to the right
        javafx.geometry.Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        popup.show(anchor, bounds.getMaxX() - 340, bounds.getMaxY() + 8);
    }

    private HBox buildNotifItem(String iconText, String iconColor, String title, String subtitle) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-cursor: hand;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: -bg-hover; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand;"));

        Label icon = new Label(iconText);
        icon.setStyle("-fx-font-size: 18px; -fx-min-width: 32; -fx-alignment: center;");

        VBox textBox = new VBox(2);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: -text-primary;");
        titleLbl.setWrapText(true);
        Label subLbl = new Label(subtitle);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-secondary;");
        subLbl.setWrapText(true);
        textBox.getChildren().addAll(titleLbl, subLbl);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        row.getChildren().addAll(icon, textBox);
        return row;
    }

    // ═══════════════════════════════════════════════════════════
    //  DASHBOARD PANEL
    // ═══════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void showDashboard() {
        headerTitle.setText("Dashboard");
        contentArea.getChildren().clear();

        VBox panel = new VBox(20);
        panel.setPadding(new Insets(0));

        // 1. Stats Bar
        HBox statsBar = buildInventoryStats();

        // 2. Charts Row
        HBox chartsRow = new HBox(20);
        chartsRow.setAlignment(Pos.CENTER_LEFT);

        List<Equipment> allEquipment = equipmentDAO.getAllEquipment();
        long available = allEquipment.stream().filter(e -> "AVAILABLE".equals(e.getEquipmentStatus())).count();
        long borrowed = allEquipment.stream().filter(e -> "BORROWED".equals(e.getEquipmentStatus())).count();
        long inMaint = allEquipment.stream().filter(e -> "IN_MAINTENANCE".equals(e.getEquipmentStatus())).count();

        // -- Custom Bar Chart: Equipment by category --
        VBox barCard = new VBox(5);
        barCard.getStyleClass().add("card-panel");
        HBox.setHgrow(barCard, Priority.ALWAYS);

        Label barTitle = new Label("Equipment by category");
        barTitle.getStyleClass().add("chart-title");
        Label barSub = new Label("All " + allEquipment.size() + " assets across categories");
        barSub.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 11px;");
        
        VBox.setMargin(barSub, new Insets(0, 0, 10, 0));

        HBox customBarChart = new HBox(12);
        customBarChart.setAlignment(Pos.BOTTOM_CENTER);
        customBarChart.setMinHeight(160);
        customBarChart.setPrefHeight(160);

        List<Category> categories = categoryDAO.getAllCategories();
        Map<Integer, Long> categoryCounts = allEquipment.stream()
            .collect(Collectors.groupingBy(Equipment::getCategoryId, Collectors.counting()));
            
        long maxCount = 0;
        for (long count : categoryCounts.values()) {
            if (count > maxCount) maxCount = count;
        }
        if (maxCount == 0) maxCount = 1;
        
        String[] barColors = {"#5b47e0", "#9b8af2", "#bfaef9", "#d5c8fa", "#eae3fb"};
        int colorIdx = 0;
        
        for (Category cat : categories) {
            long count = categoryCounts.getOrDefault(cat.getCategoryId(), 0L);
            if (count > 0) {
                VBox barContainer = new VBox(6);
                barContainer.setAlignment(Pos.BOTTOM_CENTER);
                HBox.setHgrow(barContainer, Priority.ALWAYS);
                
                Label countLbl = new Label(String.valueOf(count));
                countLbl.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -text-primary;");
                
                Region bar = new Region();
                String color = barColors[Math.min(colorIdx, barColors.length - 1)];
                bar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4 4 0 0;");
                
                double height = (double) count / maxCount * 120.0;
                if (height < 15 && count > 0) height = 15; 
                bar.setMinHeight(height);
                bar.setPrefHeight(height);
                bar.setMaxHeight(height);
                bar.setMaxWidth(Double.MAX_VALUE);
                
                Label nameLbl = new Label(cat.getCategoryName());
                nameLbl.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 10px; -fx-text-fill: -text-secondary;");
                
                barContainer.getChildren().addAll(countLbl, bar, nameLbl);
                customBarChart.getChildren().add(barContainer);
                colorIdx++;
            }
        }
        
        barCard.getChildren().addAll(barTitle, barSub, customBarChart);

        // -- Pie Chart: Status breakdown --
        VBox pieCard = new VBox(5);
        pieCard.getStyleClass().add("card-panel");
        pieCard.setMinWidth(300);

        Label pieTitle = new Label("Status breakdown");
        pieTitle.getStyleClass().add("chart-title");
        
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data("Available", available),
            new PieChart.Data("Borrowed", borrowed),
            new PieChart.Data("Maintenance", inMaint)
        );
        PieChart statusChart = new PieChart(pieData);
        statusChart.setLegendVisible(false);
        statusChart.setLabelsVisible(false);
        statusChart.setMinHeight(150);
        statusChart.setPrefHeight(150);
        statusChart.setPrefWidth(150);
        statusChart.getStyleClass().add("custom-pie-chart");
        
        StackPane donutContainer = new StackPane();
        javafx.scene.shape.Circle innerHole = new javafx.scene.shape.Circle(40);
        innerHole.setStyle("-fx-fill: -bg-card;");
        Label totalLbl = new Label(String.valueOf(allEquipment.size()));
        totalLbl.setStyle("-fx-font-family: 'Poppins'; -fx-font-weight: 700; -fx-font-size: 18px; -fx-text-fill: -text-primary;");
        donutContainer.getChildren().addAll(statusChart, innerHole, totalLbl);
        
        VBox customLegend = new VBox(12);
        customLegend.setAlignment(Pos.CENTER_LEFT);
        customLegend.getChildren().addAll(
            buildLegendItem("Available", "#06d6a0", available),
            buildLegendItem("Borrowed", "#f79009", borrowed),
            buildLegendItem("Maintenance", "#ef476f", inMaint)
        );
        
        HBox pieContent = new HBox(30);
        pieContent.setAlignment(Pos.CENTER_LEFT);
        pieContent.getChildren().addAll(donutContainer, customLegend);
        
        pieCard.getChildren().addAll(pieTitle, pieContent);
        
        chartsRow.getChildren().addAll(barCard, pieCard);

        // 3. Tables Row
        VBox tablesRow = new VBox(15);
        tablesRow.setAlignment(Pos.TOP_LEFT);

        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        Label tableTitle = new Label("Recent bookings");
        tableTitle.setStyle("-fx-font-family: 'Poppins'; -fx-font-weight: 700; -fx-font-size: 16px; -fx-text-fill: -text-primary;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        TextField searchBookings = new TextField();
        searchBookings.setPromptText("🔍 Search bookings...");
        searchBookings.getStyleClass().add("text-field");
        searchBookings.setPrefWidth(200);
        
        Button newBookingBtn = new Button("+ New booking");
        newBookingBtn.getStyleClass().add("btn-primary");
        newBookingBtn.setOnAction(e -> showAddBookingDialog());
        
        HBox.setMargin(searchBookings, new Insets(0, 10, 0, 0));
        tableHeader.getChildren().addAll(tableTitle, spacer, searchBookings, newBookingBtn);

        TableView<Booking> recentTable = new TableView<>();
        recentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        recentTable.setMinHeight(300);
        recentTable.getStyleClass().add("modern-table");

        TableColumn<Booking, String> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(c -> new SimpleStringProperty(String.format("#%03d", c.getValue().getBookingId())));
        cId.setMaxWidth(60);

        Map<Integer, String> equipmentMap = equipmentDAO.getAllEquipment().stream()
            .collect(Collectors.toMap(Equipment::getEquipmentId, Equipment::getEquipmentName, (a, b) -> a));
        Map<Integer, String> catMap = equipmentDAO.getAllEquipment().stream()
            .collect(Collectors.toMap(Equipment::getEquipmentId, e -> {
                return categoryDAO.getAllCategories().stream()
                    .filter(cat -> cat.getCategoryId() == e.getCategoryId())
                    .map(Category::getCategoryName)
                    .findFirst().orElse("Unknown");
            }, (a, b) -> a));

        TableColumn<Booking, String> cEquip = new TableColumn<>("EQUIPMENT");
        cEquip.setCellValueFactory(c -> new SimpleStringProperty(equipmentMap.getOrDefault(c.getValue().getEquipmentId(), "Unknown")));
        cEquip.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Booking b = getTableView().getItems().get(getIndex());
                    VBox box = new VBox(2);
                    Label name = new Label(item);
                    name.setStyle("-fx-font-weight: 600; -fx-text-fill: -text-primary;");
                    Label cat = new Label(catMap.getOrDefault(b.getEquipmentId(), ""));
                    cat.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 11px;");
                    
                    HBox fullBox = new HBox(10);
                    fullBox.setAlignment(Pos.CENTER_LEFT);
                    Label icon = new Label("EX");
                    icon.setStyle("-fx-background-color: #e8e6f8; -fx-text-fill: -accent; -fx-padding: 4 6; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: 800;");
                    box.getChildren().addAll(name, cat);
                    fullBox.getChildren().addAll(icon, box);
                    setGraphic(fullBox);
                }
            }
        });

        TableColumn<Booking, String> cBorrower = new TableColumn<>("BORROWER");
        UserDAO uDao = new UserDAO();
        Map<Integer, String> uMap = uDao.getUserMap();
        cBorrower.setCellValueFactory(c -> new SimpleStringProperty(uMap.getOrDefault(c.getValue().getBorrowerId(), "Unknown")));

        TableColumn<Booking, String> cStart = new TableColumn<>("START DATE");
        cStart.setCellValueFactory(c -> new SimpleStringProperty(formatDate(c.getValue().getStartDatetime())));

        TableColumn<Booking, String> cReturn = new TableColumn<>("RETURN BY");
        cReturn.setCellValueFactory(c -> new SimpleStringProperty(formatDate(c.getValue().getExpectedReturnDatetime())));
        cReturn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    Booking b = getTableView().getItems().get(getIndex());
                    if ("PENDING".equals(b.getBookingStatus()) || "APPROVED".equals(b.getBookingStatus())) {
                        try {
                            if (java.time.LocalDate.parse(b.getExpectedReturnDatetime()).isBefore(java.time.LocalDate.now())) {
                                setStyle("-fx-text-fill: -text-danger; -fx-font-weight: 600;");
                            } else {
                                setStyle("-fx-text-fill: -text-primary;");
                            }
                        } catch(Exception e) {
                            setStyle("-fx-text-fill: -text-primary;");
                        }
                    } else {
                        setStyle("-fx-text-fill: -text-primary;");
                    }
                }
            }
        });

        TableColumn<Booking, String> cPurpose = new TableColumn<>("PURPOSE");
        cPurpose.setCellValueFactory(c -> {
            String p = c.getValue().getPurposeDescription();
            return new SimpleStringProperty((p == null || p.isEmpty()) ? "—" : p);
        });

        TableColumn<Booking, String> cStatus = new TableColumn<>("STATUS");
        cStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookingStatus()));
        cStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label("● " + (item.equals("APPROVED") ? "Active" : (item.charAt(0) + item.substring(1).toLowerCase())));
                    badge.getStyleClass().add("status-badge");
                    switch (item.toUpperCase()) {
                        case "APPROVED": badge.getStyleClass().add("status-badge-active"); break;
                        case "RETURNED": badge.getStyleClass().add("status-badge-returned"); break;
                        case "PENDING": badge.getStyleClass().add("status-badge-pending"); break;
                        default: badge.getStyleClass().add("status-badge-default");
                    }
                    setGraphic(badge);
                }
            }
        });

        TableColumn<Booking, Void> cAction = new TableColumn<>("");
        cAction.setMinWidth(60);
        cAction.setMaxWidth(60);
        cAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("•••");
            {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: -text-secondary; -fx-cursor: hand; -fx-font-size: 14px;");
                btn.setOnAction(e -> {
                    Booking b = getTableView().getItems().get(getIndex());
                    showBookingDetailsDialog(b);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        recentTable.getColumns().addAll(cId, cEquip, cBorrower, cStart, cReturn, cPurpose, cStatus, cAction);
        
        List<Booking> allBookings = bookingDAO.getAllBookings();
        // Sort to show recent first
        allBookings.sort((b1, b2) -> Integer.compare(b2.getBookingId(), b1.getBookingId()));
        recentTable.setItems(FXCollections.observableArrayList(allBookings));

        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("card-panel");
        tableContainer.getChildren().addAll(tableHeader, recentTable);
        
        tablesRow.getChildren().add(tableContainer);

        panel.getChildren().addAll(statsBar, chartsRow, tablesRow);
        contentArea.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_LEFT);
    }
    
    private HBox buildLegendItem(String label, String color, long value) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px;");
        
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 12px; -fx-text-fill: -text-secondary; -fx-min-width: 80;");
        
        Label valLbl = new Label(String.valueOf(value));
        valLbl.setStyle("-fx-font-family: 'Poppins'; -fx-font-weight: 700; -fx-font-size: 13px; -fx-text-fill: -text-primary;");
        
        box.getChildren().addAll(dot, lbl, valLbl);
        return box;
    }
    
    private String formatDate(String isoDate) {
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(isoDate);
            return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"));
        } catch(Exception e) {
            return isoDate;
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  INVENTORY PANEL
    // ═══════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void showInventory() {
        headerTitle.setText("Inventory Management");
        contentArea.getChildren().clear();

        VBox panel = new VBox(20);
        panel.setPadding(new Insets(0));

        // Stats bar
        HBox statsBar = buildInventoryStats();

        // Action toolbar
        HBox toolbar = new HBox(12);
        toolbar.getStyleClass().add("action-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search equipment...");
        searchField.getStyleClass().add("search-field");
        searchField.setMinWidth(300);

        Button addBtn = new Button("+ Add Equipment");
        // Make the in-panel 'Add' button use the violet primary style by default
        addBtn.getStyleClass().addAll("button", "btn-primary");
        addBtn.setId("add-equipment");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("\u2B07 Export");
        exportBtn.getStyleClass().addAll("button", "btn-secondary");
        exportBtn.setOnAction(e -> showExportDialog());

        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.getItems().addAll("Sort: Name (A-Z)", "Sort: Name (Z-A)", "Sort: Status");
        sortBox.setValue("Sort: Name (A-Z)");
        sortBox.getStyleClass().add("form-select");
        sortBox.setStyle("-fx-pref-width: 160px;");

        toolbar.getChildren().addAll(searchField, sortBox, spacer, addBtn, exportBtn);

        // Table
        TableView<Equipment> table = new TableView<>();
        table.setId("equipment-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getStyleClass().add("modern-table");
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setMinHeight(500);

        // Load categories for mapping category ID to Category Name dynamically
        Map<Integer, String> categoryMap = categoryDAO.getAllCategories().stream()
            .collect(Collectors.toMap(Category::getCategoryId, Category::getCategoryName, (a, b) -> a));

        TableColumn<Equipment, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEquipmentName()));

        TableColumn<Equipment, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(c -> {
            int catId = c.getValue().getCategoryId();
            String catName = categoryMap.getOrDefault(catId, "Unknown (" + catId + ")");
            return new SimpleStringProperty(catName);
        });

        TableColumn<Equipment, String> colSpecs = new TableColumn<>("Specification");
        colSpecs.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTechnicalSpecifications()));
        colSpecs.setMinWidth(200);

        TableColumn<Equipment, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEquipmentStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    switch (item.toUpperCase()) {
                        case "AVAILABLE":
                            badge.getStyleClass().add("badge-available");
                            break;
                        case "BORROWED":
                            badge.getStyleClass().add("badge-borrowed");
                            break;
                        case "IN_MAINTENANCE":
                            badge.getStyleClass().add("badge-maintenance");
                            break;
                        default:
                            badge.getStyleClass().add("badge-pending");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // 3-dots action column
        TableColumn<Equipment, Void> colActions = new TableColumn<>("");
        colActions.setMaxWidth(60);
        colActions.setMinWidth(60);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnDetails = new Button("•••");
            {
                btnDetails.getStyleClass().add("btn-icon");
                btnDetails.setStyle("-fx-font-size: 16px; -fx-padding: 2 6; -fx-cursor: hand; -fx-text-fill: -text-secondary;");
                btnDetails.setTooltip(new Tooltip("Show full details"));
                btnDetails.setOnAction(e -> {
                    Equipment eq = getTableView().getItems().get(getIndex());
                    showEquipmentDetailsDialog(eq);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDetails);
                }
            }
        });

        table.getColumns().addAll(colName, colCategory, colSpecs, colStatus, colActions);

        // Load data
        ObservableList<Equipment> data = FXCollections.observableArrayList(equipmentDAO.getAllEquipment());
        
        sortBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Sort: Name (A-Z)".equals(newVal)) {
                FXCollections.sort(data, java.util.Comparator.comparing(eq -> eq.getEquipmentName() == null ? "" : eq.getEquipmentName()));
            } else if ("Sort: Name (Z-A)".equals(newVal)) {
                FXCollections.sort(data, java.util.Comparator.comparing((Equipment eq) -> eq.getEquipmentName() == null ? "" : eq.getEquipmentName()).reversed());
            } else if ("Sort: Status".equals(newVal)) {
                FXCollections.sort(data, java.util.Comparator.comparing(eq -> eq.getEquipmentStatus() == null ? "" : eq.getEquipmentStatus()));
            }
        });
        FXCollections.sort(data, java.util.Comparator.comparing(eq -> eq.getEquipmentName() == null ? "" : eq.getEquipmentName()));

        FilteredList<Equipment> filteredData = new FilteredList<>(data, p -> true);
        table.setItems(filteredData);

        // Search filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(eq -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return (eq.getSerialNumber() != null && eq.getSerialNumber().toLowerCase().contains(lower))
                    || (eq.getStorageLocation() != null && eq.getStorageLocation().toLowerCase().contains(lower))
                    || (eq.getTechnicalSpecifications() != null && eq.getTechnicalSpecifications().toLowerCase().contains(lower))
                    || (eq.getEquipmentStatus() != null && eq.getEquipmentStatus().toLowerCase().contains(lower));
            });
        });

        // Add equipment dialog
        addBtn.setOnAction(e -> showAddEquipmentDialog(data));

        panel.getChildren().addAll(statsBar, toolbar, table);
        contentArea.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_LEFT);
    }

    private void showBookingDetailsDialog(Booking booking) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Booking Details");
        dialog.initOwner(mainApp.getPrimaryStage());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("form-dialog");
        dialogPane.setStyle(
            "-fx-background-color: #1a1a2e; -fx-border-color: #2a2a45; -fx-border-radius: 16; -fx-background-radius: 16;"
        );
        dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);

        Button closeBtn = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
        if (closeBtn != null) {
            closeBtn.getStyleClass().addAll("button", "btn-primary");
            closeBtn.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        }

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setMinWidth(460);
        content.getStyleClass().add("modal-card");

        Label titleLbl = new Label("Booking Information");
        titleLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e8e8f0;");

        Label subtitleLbl = new Label("Full booking history and status details");
        subtitleLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #8888a8;");

        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 0 0 10 0;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(16);
        grid.getStyleClass().add("form-grid");

        UserDAO uDao = new UserDAO();
        String borrowerName = uDao.getUserMap().getOrDefault(booking.getBorrowerId(), "User ID: " + booking.getBorrowerId());
        
        Equipment eq = equipmentDAO.getAllEquipment().stream().filter(e -> e.getEquipmentId() == booking.getEquipmentId()).findFirst().orElse(null);
        String equipmentName = eq != null ? eq.getEquipmentName() : "Equip ID: " + booking.getEquipmentId();

        int row = 0;
        addDetailRow(grid, row++, "Booking ID", String.valueOf(booking.getBookingId()));
        addDetailRow(grid, row++, "Equipment", equipmentName);
        addDetailRow(grid, row++, "Borrower", borrowerName);
        addDetailRow(grid, row++, "Start Date", formatDate(booking.getStartDatetime()));
        addDetailRow(grid, row++, "Return Date", formatDate(booking.getExpectedReturnDatetime()));
        addDetailRow(grid, row++, "Purpose", booking.getPurposeDescription());

        Label lblStatus = new Label("Status");
        lblStatus.getStyleClass().add("form-label");
        Label valStatus = new Label(booking.getBookingStatus());
        valStatus.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: -text-primary;");
        grid.addRow(row++, lblStatus, valStatus);

        content.getChildren().addAll(titleLbl, subtitleLbl, sep, grid);
        
        // Add action buttons based on status
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(10, 0, 0, 0));
        
        if ("PENDING".equals(booking.getBookingStatus())) {
            Button approveBtn = new Button("Approve");
            approveBtn.getStyleClass().addAll("button", "btn-success");
            approveBtn.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
            approveBtn.setOnAction(e -> {
                bookingDAO.updateBookingStatus(booking.getBookingId(), "APPROVED", currentUser.getUserId(), null);
                dialog.setResult(null);
                dialog.close();
                showDashboard();
            });
            
            Button rejectBtn = new Button("Reject");
            rejectBtn.getStyleClass().addAll("button", "btn-danger");
            rejectBtn.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
            rejectBtn.setOnAction(e -> {
                bookingDAO.updateBookingStatus(booking.getBookingId(), "REJECTED", currentUser.getUserId(), "Rejected by admin");
                dialog.setResult(null);
                dialog.close();
                showDashboard();
            });
            
            actionBox.getChildren().addAll(rejectBtn, approveBtn);
            content.getChildren().add(actionBox);
        } else if ("APPROVED".equals(booking.getBookingStatus())) {
            Button returnBtn = new Button("Mark Returned");
            returnBtn.getStyleClass().addAll("button", "btn-primary");
            returnBtn.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
            returnBtn.setOnAction(e -> {
                dialog.setResult(null);
                dialog.close();
                showReturnDialog(booking);
            });
            
            actionBox.getChildren().add(returnBtn);
            content.getChildren().add(actionBox);
        }

        dialogPane.setContent(content);

        dialog.showAndWait();
    }

    private void showEquipmentDetailsDialog(Equipment eq) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Equipment Details");
        dialog.initOwner(mainApp.getPrimaryStage());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("form-dialog");
        dialogPane.setStyle(
            "-fx-background-color: #1a1a2e; -fx-border-color: #2a2a45; -fx-border-radius: 16; -fx-background-radius: 16;"
        );
        dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);

        // Style OK/Close button
        Button closeBtn = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
        if (closeBtn != null) {
            closeBtn.getStyleClass().addAll("button", "btn-primary");
            // Keep padding/cursor here; color comes from the btn-primary CSS class
            closeBtn.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        }

        Map<Integer, String> categoryMap = categoryDAO.getAllCategories().stream()
            .collect(Collectors.toMap(Category::getCategoryId, Category::getCategoryName, (a, b) -> a));
        String categoryName = categoryMap.getOrDefault(eq.getCategoryId(), "Unknown (" + eq.getCategoryId() + ")");

        String assignedUserName = "Not Assigned";
        if (eq.getAssignedTo() != null && !eq.getAssignedTo().isEmpty()) {
            assignedUserName = eq.getAssignedTo();
        }

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setMinWidth(460);
        content.getStyleClass().add("modal-card");

        Label titleLbl = new Label("Equipment Specifications");
        titleLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e8e8f0;");

        Label subtitleLbl = new Label("Asset tracking details and database records");
        subtitleLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #8888a8;");

        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 0 0 10 0;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(16);
        grid.getStyleClass().add("form-grid");

        int row = 0;
        addDetailRow(grid, row++, "Asset ID", String.valueOf(eq.getEquipmentId()));
        addDetailRow(grid, row++, "Name", eq.getEquipmentName() != null ? eq.getEquipmentName() : "N/A");
        addDetailRow(grid, row++, "Serial Number", eq.getSerialNumber());
        addDetailRow(grid, row++, "Category", categoryName);

        // Status badge
        Label lblStatus = new Label("Status");
        lblStatus.getStyleClass().add("form-label");
        lblStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-secondary;");

        Label badge = new Label(eq.getEquipmentStatus());
        badge.getStyleClass().add("badge");
        switch (eq.getEquipmentStatus().toUpperCase()) {
            case "AVAILABLE":
                badge.getStyleClass().add("badge-available");
                break;
            case "BORROWED":
                badge.getStyleClass().add("badge-borrowed");
                break;
            case "IN_MAINTENANCE":
                badge.getStyleClass().add("badge-maintenance");
                break;
            default:
                badge.getStyleClass().add("badge-pending");
        }
        grid.add(lblStatus, 0, row);
        grid.add(badge, 1, row);
        row++;

        addDetailRow(grid, row++, "Storage Location", eq.getStorageLocation() != null ? eq.getStorageLocation() : "N/A");
        addDetailRow(grid, row++, "Technical Specs", eq.getTechnicalSpecifications() != null ? eq.getTechnicalSpecifications() : "N/A");
        addDetailRow(grid, row++, "Purchase Cost", String.format("\u20B1%.2f", eq.getPurchaseCost()));
        addDetailRow(grid, row++, "Purchase Date", eq.getPurchaseDate() != null ? eq.getPurchaseDate() : "N/A");
        addDetailRow(grid, row++, "Assigned Into:", assignedUserName);

        // Action Buttons
        HBox actionBox = new HBox(12);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(16, 0, 0, 0));
        
        Button editBtn = new Button("Edit Details");
        editBtn.getStyleClass().addAll("button", "btn-primary");
        editBtn.setOnAction(e -> {
            dialog.close();
            showEditEquipmentDialog(eq);
        });
        
        Button deleteBtn = new Button("Delete Equipment");
        deleteBtn.getStyleClass().addAll("button", "btn-logout");
        deleteBtn.setStyle("-fx-border-color: #ef476f; -fx-text-fill: #ef476f;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Equipment");
            confirm.setHeaderText("Are you sure you want to delete this equipment?");
            confirm.setContentText("This action cannot be undone. " + eq.getEquipmentName());
            
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (equipmentDAO.deleteEquipment(eq.getEquipmentId())) {
                        changeLogDAO.logChange("Inventory", eq.getEquipmentId(), eq.getEquipmentName(),
                            "Equipment Deleted", eq.getSerialNumber(), "DELETED", currentUser.getUserId());
                        dialog.close();
                        showInventory();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Could not delete equipment. It may be linked to existing bookings.");
                    }
                }
            });
        });
        
        actionBox.getChildren().addAll(editBtn, deleteBtn);

        content.getChildren().addAll(titleLbl, subtitleLbl, sep, grid, actionBox);
        dialogPane.setContent(content);

        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("form-label");
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-secondary;");

        Label val = new Label(value);
        val.setStyle("-fx-text-fill: -text-primary; -fx-font-size: 14px;");
        val.setWrapText(true);

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private HBox buildInventoryStats() {
        List<Equipment> allEquipment = equipmentDAO.getAllEquipment();
        long total = allEquipment.size();
        long available = allEquipment.stream().filter(e -> "AVAILABLE".equals(e.getEquipmentStatus())).count();
        long borrowed = allEquipment.stream().filter(e -> "BORROWED".equals(e.getEquipmentStatus())).count();
        long inMaint = allEquipment.stream().filter(e -> "IN_MAINTENANCE".equals(e.getEquipmentStatus())).count();

        HBox statsBar = new HBox(20);
        statsBar.setPadding(new Insets(0, 0, 10, 0));

        // Get some mock data for badges since we don't have historical data in the DAO
        long availablePct = total > 0 ? (available * 100 / total) : 0;
        
        statsBar.getChildren().addAll(
            buildStatCard("TOTAL EQUIPMENT", String.valueOf(total), "", "total", "💼"),
            buildStatCard("AVAILABLE", String.valueOf(available), availablePct + "% of fleet", "available", "✓"),
            buildStatCard("BORROWED", String.valueOf(borrowed), borrowed + " overdue", "borrowed", "♡"),
            buildStatCard("IN MAINTENANCE", String.valueOf(inMaint), "Needs attention", "maintenance", "⏱")
        );
        return statsBar;
    }

    private VBox buildStatCard(String title, String value, String badgeText, String type, String iconChar) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("metric-card", "metric-card-" + type);
        HBox.setHgrow(card, Priority.ALWAYS);
        
        // Top Icon
        Label icon = new Label(iconChar);
        icon.getStyleClass().addAll("metric-icon", "metric-icon-" + type);
        
        // Title
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("metric-title");
        
        // Value
        Label valLbl = new Label(value);
        valLbl.getStyleClass().addAll("metric-value", "metric-value-" + type);
        
        // Badge
        if (badgeText != null && !badgeText.trim().isEmpty()) {
            Label badge = new Label(badgeText);
            badge.getStyleClass().addAll("metric-badge", "metric-badge-" + type);
            card.getChildren().addAll(icon, titleLbl, valLbl, badge);
        } else {
            card.getChildren().addAll(icon, titleLbl, valLbl);
        }
        return card;
    }

    private void showAddEquipmentDialog(ObservableList<Equipment> data) {
        Dialog<Equipment> dialog = new Dialog<>();
        dialog.setTitle("Add New Equipment");
        dialog.initOwner(mainApp.getPrimaryStage());

        DialogPane dialogPane = dialog.getDialogPane();
        // Apply theme-aware dialog class so styles.css can style the dialog card
        dialogPane.getStyleClass().add("form-dialog");
        dialogPane.setStyle(
            "-fx-background-color: #1a1a2e; -fx-border-color: #2a2a45; -fx-border-radius: 12; -fx-background-radius: 12;"
        );

        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button maintOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (maintOk != null) {
            maintOk.getStyleClass().addAll("button", "btn-primary");
            maintOk.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        }
        // Ensure OK buttons use primary (violet) style consistently
        Button addEqOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (addEqOk != null) {
            addEqOk.getStyleClass().addAll("button", "btn-primary");
            addEqOk.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        }

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(24));
        grid.getStyleClass().add("form-grid");

        TextField nameField = new TextField();
        nameField.setPromptText("Equipment Name");
        nameField.getStyleClass().add("form-input");

        TextField serialField = new TextField();
        serialField.setPromptText("Serial Number");
        serialField.getStyleClass().add("form-input");

        ComboBox<String> categoryBox = new ComboBox<>();
        List<String> catNames = categoryDAO.getAllCategories().stream()
                .map(Category::getCategoryName)
                .collect(Collectors.toList());
        if (!catNames.contains("Electronics")) {
            catNames.add(0, "Electronics");
        }
        categoryBox.setItems(FXCollections.observableArrayList(catNames));
        categoryBox.setEditable(true);
        categoryBox.setPromptText("Select or Type Category");
        categoryBox.getStyleClass().add("form-select");
        TextField specsField = new TextField();
        specsField.setPromptText("Technical Specifications");
        specsField.getStyleClass().add("form-input");

        TextField locationField = new TextField();
        locationField.setPromptText("Storage Location");
        locationField.getStyleClass().add("form-input");

        TextField costField = new TextField();
        costField.setPromptText("Purchase Cost");
        costField.getStyleClass().add("form-input");

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(java.time.LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.getStyleClass().add("form-input");

        TextField assignedToField = new TextField();
        assignedToField.setPromptText("Assigned To (Optional)");
        assignedToField.getStyleClass().add("form-input");

        Label lbl0 = new Label("Name");            lbl0.getStyleClass().add("form-label");
        Label lbl1 = new Label("Serial Number");  lbl1.getStyleClass().add("form-label");
        Label lbl2 = new Label("Category");        lbl2.getStyleClass().add("form-label");
        Label lbl3 = new Label("Specifications");  lbl3.getStyleClass().add("form-label");
        Label lbl4 = new Label("Location");        lbl4.getStyleClass().add("form-label");
        Label lbl5 = new Label("Purchase Cost");   lbl5.getStyleClass().add("form-label");
        Label lbl6 = new Label("Purchase Date");   lbl6.getStyleClass().add("form-label");
        Label lbl7 = new Label("Assigned To");     lbl7.getStyleClass().add("form-label");

        grid.addRow(0, lbl0, nameField);
        grid.addRow(1, lbl1, serialField);
        grid.addRow(2, lbl2, categoryBox);
        grid.addRow(3, lbl3, specsField);
        grid.addRow(4, lbl4, locationField);
        grid.addRow(5, lbl5, costField);
        grid.addRow(6, lbl6, datePicker);
        grid.addRow(7, lbl7, assignedToField);

        dialogPane.setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Equipment eq = new Equipment();
                eq.setEquipmentName(nameField.getText());
                eq.setSerialNumber(serialField.getText());
                String catName = categoryBox.getValue();
                int catId = 0;
                if (catName != null && !catName.trim().isEmpty()) {
                    catId = categoryDAO.getOrCreateCategory(catName.trim());
                }
                eq.setCategoryId(catId);
                eq.setTechnicalSpecifications(specsField.getText());
                eq.setStorageLocation(locationField.getText());
                try {
                    eq.setPurchaseCost(Double.parseDouble(costField.getText()));
                } catch (NumberFormatException ex) {
                    eq.setPurchaseCost(0);
                }
                String dateStr = datePicker.getValue() != null ? datePicker.getValue().toString() : java.time.LocalDate.now().toString();
                eq.setPurchaseDate(dateStr);
                eq.setEquipmentStatus("AVAILABLE");
                
                String assignedTxt = assignedToField.getText().trim();
                if (!assignedTxt.isEmpty()) {
                    eq.setAssignedTo(assignedTxt);
                } else {
                    eq.setAssignedTo(null);
                }
                
                return eq;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(eq -> {
            if (equipmentDAO.addEquipment(eq)) {
                // Log the addition as a change
                List<Equipment> refreshed = equipmentDAO.getAllEquipment();
                // Find the newly added equipment by serial number
                Equipment added = refreshed.stream()
                    .filter(e -> e.getSerialNumber() != null && e.getSerialNumber().equals(eq.getSerialNumber()))
                    .findFirst().orElse(null);
                if (added != null) {
                    changeLogDAO.logChange("Inventory", added.getEquipmentId(), added.getEquipmentName(),
                        "New Equipment Added", "—", "Serial: " + added.getSerialNumber() + ", Status: " + added.getEquipmentStatus(),
                        currentUser.getUserId());
                }
                data.setAll(refreshed);
                showInventory(); // Refresh stats too
            }
        });
    }

    private void showEditEquipmentDialog(Equipment eq) {
        Dialog<Equipment> dialog = new Dialog<>();
        dialog.setTitle("Edit Equipment");
        dialog.initOwner(mainApp.getPrimaryStage());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("form-dialog");
        dialogPane.setStyle(
            "-fx-background-color: #1a1a2e; -fx-border-color: #2a2a45; -fx-border-radius: 12; -fx-background-radius: 12;"
        );

        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        // Make Change Password OK button primary
        Button cpOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (cpOk != null) {
            cpOk.getStyleClass().addAll("button", "btn-primary");
            cpOk.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        }
        // Make dialog OK use primary (violet)
        Button editOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (editOk != null) {
            editOk.getStyleClass().addAll("button", "btn-primary");
            editOk.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        }

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(24));
        grid.getStyleClass().add("form-grid");

        TextField nameField = new TextField();
        nameField.setPromptText("Equipment Name");
        nameField.setText(eq.getEquipmentName());

        TextField serialField = new TextField();
        serialField.setPromptText("Serial Number");
        serialField.setText(eq.getSerialNumber());

        ComboBox<String> categoryBox = new ComboBox<>();
        List<String> catNames = categoryDAO.getAllCategories().stream()
                .map(Category::getCategoryName)
                .collect(Collectors.toList());
        if (!catNames.contains("Electronics")) {
            catNames.add(0, "Electronics");
        }
        categoryBox.setItems(FXCollections.observableArrayList(catNames));
        categoryBox.setEditable(true);
        categoryBox.setPromptText("Select or Type Category");
        
        String currentCatName = "Unknown";
        for (Category c : categoryDAO.getAllCategories()) {
            if (c.getCategoryId() == eq.getCategoryId()) {
                currentCatName = c.getCategoryName();
                break;
            }
        }
        categoryBox.setValue(currentCatName);

        TextField specsField = new TextField();
        specsField.setPromptText("Technical Specifications");
        specsField.setText(eq.getTechnicalSpecifications());

        TextField locationField = new TextField();
        locationField.setPromptText("Storage Location");
        locationField.setText(eq.getStorageLocation());

        TextField costField = new TextField();
        costField.setPromptText("Purchase Cost");
        costField.setText(String.valueOf(eq.getPurchaseCost()));

        DatePicker datePicker = new DatePicker();
        try {
            datePicker.setValue(java.time.LocalDate.parse(eq.getPurchaseDate()));
        } catch (Exception ex) {
            datePicker.setValue(java.time.LocalDate.now());
        }
        datePicker.setMaxWidth(Double.MAX_VALUE);

        TextField assignedToField = new TextField();
        assignedToField.setPromptText("Assigned To (Optional)");
        if (eq.getAssignedTo() != null) {
            assignedToField.setText(eq.getAssignedTo());
        }

        Label lbl0 = new Label("Name");            lbl0.getStyleClass().add("form-label");
        Label lbl1 = new Label("Serial Number");  lbl1.getStyleClass().add("form-label");
        Label lbl2 = new Label("Category");        lbl2.getStyleClass().add("form-label");
        Label lbl3 = new Label("Specifications");  lbl3.getStyleClass().add("form-label");
        Label lbl4 = new Label("Location");        lbl4.getStyleClass().add("form-label");
        Label lbl5 = new Label("Purchase Cost");   lbl5.getStyleClass().add("form-label");
        Label lbl6 = new Label("Purchase Date");   lbl6.getStyleClass().add("form-label");
        Label lbl7 = new Label("Assigned To");     lbl7.getStyleClass().add("form-label");

        grid.addRow(0, lbl0, nameField);
        grid.addRow(1, lbl1, serialField);
        grid.addRow(2, lbl2, categoryBox);
        grid.addRow(3, lbl3, specsField);
        grid.addRow(4, lbl4, locationField);
        grid.addRow(5, lbl5, costField);
        grid.addRow(6, lbl6, datePicker);
        grid.addRow(7, lbl7, assignedToField);

        dialogPane.setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                eq.setEquipmentName(nameField.getText());
                eq.setSerialNumber(serialField.getText());
                String catName = categoryBox.getValue();
                int catId = 0;
                if (catName != null && !catName.trim().isEmpty()) {
                    catId = categoryDAO.getOrCreateCategory(catName.trim());
                }
                eq.setCategoryId(catId);
                eq.setTechnicalSpecifications(specsField.getText());
                eq.setStorageLocation(locationField.getText());
                try {
                    eq.setPurchaseCost(Double.parseDouble(costField.getText()));
                } catch (NumberFormatException ex) {
                    eq.setPurchaseCost(0);
                }
                String dateStr = datePicker.getValue() != null ? datePicker.getValue().toString() : java.time.LocalDate.now().toString();
                eq.setPurchaseDate(dateStr);
                
                String assignedTxt = assignedToField.getText().trim();
                if (!assignedTxt.isEmpty()) {
                    eq.setAssignedTo(assignedTxt);
                } else {
                    eq.setAssignedTo(null);
                }
                
                return eq;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedEq -> {
            if (equipmentDAO.updateEquipment(updatedEq)) {
                changeLogDAO.logChange("Inventory", updatedEq.getEquipmentId(), updatedEq.getEquipmentName(),
                    "Equipment Edited", "—", "Updated details",
                    currentUser.getUserId());
                showInventory(); // Refresh view
                // Re-open details dialog to show updated info
                showEquipmentDetailsDialog(updatedEq);
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Could not update equipment details.");
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  LOAN APPROVALS PANEL
    // ═══════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void showLoanApprovals() {
        headerTitle.setText("Booking");
        contentArea.getChildren().clear();

        // Load lookup maps dynamically
        Map<Integer, String> equipmentMap = equipmentDAO.getAllEquipment().stream()
            .collect(Collectors.toMap(Equipment::getEquipmentId, Equipment::getSerialNumber, (a, b) -> a));
        UserDAO userDAO = new UserDAO();
        Map<Integer, String> userMap = userDAO.getUserMap();

        VBox panel = new VBox(20);
        panel.setPadding(new Insets(0));

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.getStyleClass().add("action-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Review and manage equipment booking requests");
        subtitle.getStyleClass().add("content-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        // Use violet primary for booking add button as requested
        addBtn.getStyleClass().addAll("button", "btn-primary");
        addBtn.setId("add-booking");
        addBtn.setOnAction(e -> showAddBookingDialog());

        Button exportBtn = new Button("\u2B07 Export");
        exportBtn.getStyleClass().addAll("button", "btn-secondary");
        exportBtn.setOnAction(e -> showExportDialog());

        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.getItems().addAll("Sort: Newest First", "Sort: Oldest First", "Sort: Status");
        sortBox.setValue("Sort: Newest First");
        sortBox.getStyleClass().add("form-select");
        sortBox.setStyle("-fx-pref-width: 160px;");

        toolbar.getChildren().addAll(subtitle, sortBox, spacer, addBtn, exportBtn);

        // Table
        TableView<Booking> table = new TableView<>();
        table.setId("bookings-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getStyleClass().add("modern-table");
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setMinHeight(500);

        TableColumn<Booking, Integer> colId = new TableColumn<>("Id");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getBookingId()).asObject());
        colId.setMaxWidth(70);

        TableColumn<Booking, String> colEquipName = new TableColumn<>("Equipment Name");
        colEquipName.setCellValueFactory(c -> {
            int equipId = c.getValue().getEquipmentId();
            String name = equipmentMap.getOrDefault(equipId, "Unknown (" + equipId + ")");
            return new SimpleStringProperty(name);
        });

        TableColumn<Booking, String> colBorrowerName = new TableColumn<>("Borrower Name");
        colBorrowerName.setCellValueFactory(c -> {
            int borrowerId = c.getValue().getBorrowerId();
            String name = userMap.getOrDefault(borrowerId, "Unknown (" + borrowerId + ")");
            return new SimpleStringProperty(name);
        });

        TableColumn<Booking, String> colStart = new TableColumn<>("Start Date");
        colStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStartDatetime()));

        TableColumn<Booking, String> colReturnAt = new TableColumn<>("Return At");
        colReturnAt.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getExpectedReturnDatetime()));

        TableColumn<Booking, String> colPurpose = new TableColumn<>("Purpose");
        colPurpose.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPurposeDescription()));
        colPurpose.setMinWidth(200);

        TableColumn<Booking, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookingStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    switch (item.toUpperCase()) {
                        case "PENDING":   badge.getStyleClass().add("badge-pending"); break;
                        case "QUEUE":     badge.getStyleClass().add("badge-pending"); badge.setStyle("-fx-background-color: rgba(255,165,0,0.2); -fx-text-fill: #ffa500;"); break;
                        case "APPROVED":  badge.getStyleClass().add("badge-approved"); break;
                        case "DECLINED":  
                        case "REJECTED":  badge.getStyleClass().add("badge-rejected"); break;
                        case "RETURNED":  badge.getStyleClass().add("badge-returned"); break;
                        default:          badge.getStyleClass().add("badge-pending");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<Booking, Void> colActions = new TableColumn<>("Actions");
        colActions.setMinWidth(220);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox actionBox = new HBox(8, approveBtn, rejectBtn);

            private final Button qApproveBtn = new Button("APPROVE");
            private final Button qDeclineBtn = new Button("DECLINE");
            private final HBox qActionBox = new HBox(8, qApproveBtn, qDeclineBtn);

            private final Button returnBtn = new Button("Return");
            private final HBox returnBox = new HBox(returnBtn);

            {
                approveBtn.getStyleClass().addAll("button", "btn-success");
                approveBtn.setStyle("-fx-padding: 6 16; -fx-font-size: 12px;");
                rejectBtn.getStyleClass().addAll("button", "btn-danger");
                rejectBtn.setStyle("-fx-padding: 6 16; -fx-font-size: 12px;");
                actionBox.setAlignment(Pos.CENTER);

                qApproveBtn.getStyleClass().addAll("button", "btn-success");
                qApproveBtn.setStyle("-fx-padding: 6 16; -fx-font-size: 12px;");
                qDeclineBtn.getStyleClass().addAll("button", "btn-danger");
                qDeclineBtn.setStyle("-fx-padding: 6 16; -fx-font-size: 12px;");
                qActionBox.setAlignment(Pos.CENTER);

                returnBtn.getStyleClass().addAll("button", "btn-primary");
                returnBtn.setStyle("-fx-padding: 6 16; -fx-font-size: 12px; -fx-background-color: linear-gradient(to right, #4361ee, #3a86ff); -fx-text-fill: white; -fx-cursor: hand;");
                returnBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Booking booking = getTableView().getItems().get(getIndex());
                    if ("PENDING".equals(booking.getBookingStatus())) {
                        approveBtn.setOnAction(e -> {
                            bookingDAO.updateBookingStatus(booking.getBookingId(), "APPROVED", currentUser.getUserId(), null);
                            changeLogDAO.logChange("Booking", booking.getBookingId(),
                                "Booking #" + booking.getBookingId(),
                                "Status", "PENDING", "APPROVED", currentUser.getUserId());
                            showLoanApprovals();
                        });
                        rejectBtn.setOnAction(e -> {
                            bookingDAO.updateBookingStatus(booking.getBookingId(), "REJECTED", currentUser.getUserId(), "Rejected by admin");
                            changeLogDAO.logChange("Booking", booking.getBookingId(),
                                "Booking #" + booking.getBookingId(),
                                "Status", "PENDING", "REJECTED", currentUser.getUserId());
                            showLoanApprovals();
                        });
                        setGraphic(actionBox);
                    } else if ("QUEUE".equals(booking.getBookingStatus())) {
                        qApproveBtn.setOnAction(e -> {
                            bookingDAO.updateBookingStatus(booking.getBookingId(), "APPROVED", currentUser.getUserId(), null);
                            changeLogDAO.logChange("Booking", booking.getBookingId(),
                                "Booking #" + booking.getBookingId(),
                                "Status", "QUEUE", "APPROVED", currentUser.getUserId());
                            showLoanApprovals();
                        });
                        qDeclineBtn.setOnAction(e -> {
                            bookingDAO.updateBookingStatus(booking.getBookingId(), "DECLINED", currentUser.getUserId(), "Declined reservation");
                            changeLogDAO.logChange("Booking", booking.getBookingId(),
                                "Booking #" + booking.getBookingId(),
                                "Status", "QUEUE", "DECLINED", currentUser.getUserId());
                            showLoanApprovals();
                        });
                        setGraphic(qActionBox);
                    } else if ("APPROVED".equals(booking.getBookingStatus())) {
                        returnBtn.setOnAction(e -> {
                            showReturnDialog(booking);
                            changeLogDAO.logChange("Booking", booking.getBookingId(),
                                "Booking #" + booking.getBookingId(),
                                "Status", "APPROVED", "RETURNED", currentUser.getUserId());
                        });
                        setGraphic(returnBox);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        table.getColumns().addAll(colId, colEquipName, colBorrowerName, colStart, colReturnAt, colPurpose, colStatus, colActions);
        ObservableList<Booking> data = FXCollections.observableArrayList(bookingDAO.getAllBookings());
        sortBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Sort: Newest First".equals(newVal)) {
                FXCollections.sort(data, java.util.Comparator.comparing((Booking b) -> b.getStartDatetime() == null ? "" : b.getStartDatetime()).reversed());
            } else if ("Sort: Oldest First".equals(newVal)) {
                FXCollections.sort(data, java.util.Comparator.comparing(b -> b.getStartDatetime() == null ? "" : b.getStartDatetime()));
            } else if ("Sort: Status".equals(newVal)) {
                FXCollections.sort(data, java.util.Comparator.comparing(b -> b.getBookingStatus() == null ? "" : b.getBookingStatus()));
            }
        });
        FXCollections.sort(data, java.util.Comparator.comparing((Booking b) -> b.getStartDatetime() == null ? "" : b.getStartDatetime()).reversed());
        table.setItems(data);

        panel.getChildren().addAll(toolbar, table);
        contentArea.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_LEFT);
    }

    private void showReturnDialog(Booking booking) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Process Equipment Return");
        dialog.initOwner(mainApp.getPrimaryStage());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("form-dialog");
        dialogPane.setStyle(
            "-fx-background-color: #1a1a2e; -fx-border-color: #2a2a45; -fx-border-radius: 12; -fx-background-radius: 12;"
        );
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        // Make User Settings OK button primary
        Button usOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (usOk != null) {
            usOk.getStyleClass().addAll("button", "btn-primary");
            usOk.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        }

        // Style dialog buttons: make OK use primary (violet) style
        Button okBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okBtn != null) {
            okBtn.getStyleClass().addAll("button", "btn-primary");
            // keep padding and cursor via inline style, let CSS provide color
            okBtn.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        }
        Button cancelBtn = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        if (cancelBtn != null) {
            cancelBtn.getStyleClass().addAll("button", "btn-logout");
            cancelBtn.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-border-color: -border; -fx-text-fill: -text-secondary;");
        }

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(24));
        grid.getStyleClass().add("form-grid");

        Label title = new Label("Record Equipment Return");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -text-primary;");
        
        DatePicker returnDatePicker = new DatePicker();
        returnDatePicker.setValue(java.time.LocalDate.now());
        returnDatePicker.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> conditionBox = new ComboBox<>();
        conditionBox.setItems(FXCollections.observableArrayList("EXCELLENT", "GOOD", "DAMAGED", "NEEDS_REPAIR"));
        conditionBox.setValue("GOOD");
        conditionBox.setMaxWidth(Double.MAX_VALUE);

        Label l1 = new Label("Return Date");
        l1.getStyleClass().add("form-label");
        Label l2 = new Label("Condition");
        l2.getStyleClass().add("form-label");

        grid.add(title, 0, 0, 2, 1);
        grid.addRow(1, l1, returnDatePicker);
        grid.addRow(2, l2, conditionBox);

        dialogPane.setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String returnDate = returnDatePicker.getValue() != null ? returnDatePicker.getValue().toString() : java.time.LocalDate.now().toString();
                String condition = conditionBox.getValue() != null ? conditionBox.getValue() : "GOOD";
                return bookingDAO.recordReturn(booking.getBookingId(), returnDate, condition);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                showLoanApprovals();
            }
        });
    }

    private void showAddBookingDialog() {
        Dialog<Booking> dialog = new Dialog<>();
        dialog.setTitle("Add New Booking");
        dialog.initOwner(mainApp.getPrimaryStage());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #1a1a2e; -fx-border-color: #2a2a45; -fx-border-radius: 12; -fx-background-radius: 12;"
        );
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style buttons: OK should be primary (violet) to match app theme
        Button okBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okBtn != null) {
            okBtn.getStyleClass().addAll("button", "btn-primary");
            okBtn.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        }
        Button cancelBtn = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        if (cancelBtn != null) {
            cancelBtn.getStyleClass().addAll("button", "btn-logout");
            cancelBtn.setStyle("-fx-padding: 8 20; -fx-cursor: hand; -fx-border-color: -border; -fx-text-fill: -text-secondary;");
        }

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(24));

        TextField borrowerField = new TextField();
        borrowerField.setPromptText("Enter Borrower Username...");
        borrowerField.setMaxWidth(Double.MAX_VALUE);
        borrowerField.getStyleClass().add("form-input");

        ComboBox<Equipment> equipmentBox = new ComboBox<>();
        equipmentBox.setEditable(true);
        equipmentBox.setPromptText("Type to search available equipment...");
        equipmentBox.setMaxWidth(Double.MAX_VALUE);
        equipmentBox.getStyleClass().add("form-select");

        UserDAO userDAO = new UserDAO();
        List<Equipment> availableEquip = equipmentDAO.getAllEquipment();
        availableEquip.removeIf(eq -> !"AVAILABLE".equals(eq.getEquipmentStatus()));
        ObservableList<Equipment> originalList = FXCollections.observableArrayList(availableEquip);
        equipmentBox.setItems(originalList);

        equipmentBox.setConverter(new javafx.util.StringConverter<Equipment>() {
            @Override
            public String toString(Equipment eq) {
                return eq == null ? "" : (eq.getEquipmentName() + " (" + eq.getTechnicalSpecifications() + ")");
            }

            @Override
            public Equipment fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                String trimmed = string.trim();
                return availableEquip.stream()
                    .filter(eq -> {
                        String fullDisplay = eq.getEquipmentName() + " (" + eq.getTechnicalSpecifications() + ")";
                        return fullDisplay.equalsIgnoreCase(trimmed) || 
                               (eq.getEquipmentName() != null && eq.getEquipmentName().equalsIgnoreCase(trimmed));
                    })
                    .findFirst()
                    .orElse(null);
            }
        });

        equipmentBox.setCellFactory(col -> new ListCell<>() {
            @Override
            protected void updateItem(Equipment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getEquipmentName() + " (" + item.getTechnicalSpecifications() + ")");
                }
            }
        });

        equipmentBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                equipmentBox.setItems(originalList);
                equipmentBox.hide();
            } else {
                Equipment selected = equipmentBox.getSelectionModel().getSelectedItem();
                String trimmed = newVal.trim();
                if (selected != null) {
                    String fullDisplay = selected.getEquipmentName() + " (" + selected.getTechnicalSpecifications() + ")";
                    if (fullDisplay.equalsIgnoreCase(trimmed) || 
                        (selected.getEquipmentName() != null && selected.getEquipmentName().equalsIgnoreCase(trimmed))) {
                        return;
                    }
                }
                ObservableList<Equipment> filteredList = FXCollections.observableArrayList();
                for (Equipment eq : originalList) {
                    if ((eq.getEquipmentName() != null && eq.getEquipmentName().toLowerCase().contains(newVal.toLowerCase())) ||
                        (eq.getSerialNumber() != null && eq.getSerialNumber().toLowerCase().contains(newVal.toLowerCase())) ||
                        (eq.getTechnicalSpecifications() != null && eq.getTechnicalSpecifications().toLowerCase().contains(newVal.toLowerCase()))) {
                        filteredList.add(eq);
                    }
                }
                equipmentBox.setItems(filteredList);
                if (!filteredList.isEmpty()) {
                    equipmentBox.show();
                } else {
                    equipmentBox.hide();
                }
            }
        });

        DatePicker startDate = new DatePicker();
        startDate.setValue(java.time.LocalDate.now());
        startDate.setMaxWidth(Double.MAX_VALUE);
        startDate.getStyleClass().add("form-input");

        DatePicker returnDate = new DatePicker();
        returnDate.setValue(java.time.LocalDate.now().plusDays(7));
        returnDate.setMaxWidth(Double.MAX_VALUE);
        returnDate.getStyleClass().add("form-input");

        TextArea purposeField = new TextArea();
        purposeField.setPromptText("Describe the booking purpose...");
        purposeField.setPrefRowCount(3);
        purposeField.getStyleClass().add("form-textarea");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Borrow", "Reserve");
        typeBox.setValue("Borrow");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.getStyleClass().add("form-select");

        Label costLabel = new Label("Cost (₱)");
        costLabel.getStyleClass().add("form-label");
        costLabel.setVisible(false);
        costLabel.setManaged(false);

        TextField costField = new TextField();
        costField.setPromptText("Enter amount in PHP");
        costField.getStyleClass().add("form-input");
        costField.setVisible(false);
        costField.setManaged(false);

        typeBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isReserve = "Reserve".equals(newVal);
            costLabel.setVisible(isReserve);
            costLabel.setManaged(isReserve);
            costField.setVisible(isReserve);
            costField.setManaged(isReserve);
        });

        Label lbl0 = new Label("Type");         lbl0.getStyleClass().add("form-label");
        Label lbl1 = new Label("Borrower");     lbl1.getStyleClass().add("form-label");
        Label lbl2 = new Label("Equipment");    lbl2.getStyleClass().add("form-label");
        Label lbl3 = new Label("Start Date");   lbl3.getStyleClass().add("form-label");
        Label lbl4 = new Label("Return Date");  lbl4.getStyleClass().add("form-label");
        Label lbl5 = new Label("Purpose");      lbl5.getStyleClass().add("form-label");

        grid.addRow(0, lbl0, typeBox);
        grid.addRow(1, costLabel, costField);
        grid.addRow(2, lbl1, borrowerField);
        grid.addRow(3, lbl2, equipmentBox);
        grid.addRow(4, lbl3, startDate);
        grid.addRow(5, lbl4, returnDate);
        grid.addRow(6, lbl5, purposeField);

        dialogPane.setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String borrowerUsername = borrowerField.getText().trim();
                User targetUser = userDAO.getUserByUsername(borrowerUsername);
                
                if (targetUser == null && !borrowerUsername.isEmpty()) {
                    User newUser = new User();
                    newUser.setUsername(borrowerUsername);
                    newUser.setPasswordHash(tracker.util.SecurityUtil.hashPassword("password"));
                    newUser.setUserRole("BORROWER");
                    if (userDAO.registerUser(newUser)) {
                        targetUser = userDAO.getUserByUsername(borrowerUsername);
                    }
                }

                Equipment selectedEquip = equipmentBox.getValue();
                if (selectedEquip == null) {
                    String enteredText = equipmentBox.getEditor().getText().trim();
                    selectedEquip = availableEquip.stream()
                        .filter(eq -> {
                            String fullDisplay = eq.getEquipmentName() + " (" + eq.getTechnicalSpecifications() + ")";
                            return fullDisplay.equalsIgnoreCase(enteredText) || 
                                   (eq.getEquipmentName() != null && eq.getEquipmentName().equalsIgnoreCase(enteredText));
                        })
                        .findFirst()
                        .orElse(null);
                }

                if (targetUser == null || selectedEquip == null) {
                    return null;
                }

                Booking b = new Booking();
                b.setBorrowerId(targetUser.getUserId());
                b.setEquipmentId(selectedEquip.getEquipmentId());
                b.setStartDatetime(startDate.getValue() != null ? startDate.getValue().toString() : java.time.LocalDate.now().toString());
                b.setExpectedReturnDatetime(returnDate.getValue() != null ? returnDate.getValue().toString() : java.time.LocalDate.now().plusDays(7).toString());
                b.setPurposeDescription(purposeField.getText().trim());
                if ("Reserve".equals(typeBox.getValue())) {
                    b.setBookingStatus("QUEUE");
                    try {
                        b.setBorrowingPrice(Double.parseDouble(costField.getText().trim()));
                    } catch (NumberFormatException ex) {
                        b.setBorrowingPrice(0.0);
                    }
                } else {
                    b.setBookingStatus("APPROVED"); // Admins pre-approve
                }
                return b;
            }
            return null;
        });

        // Prevent dialog from closing if input validation fails
        dialog.setOnShowing(dialogEvent -> {
            Button actualOkBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
            actualOkBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                String borrowerUsername = borrowerField.getText().trim();
                if (borrowerUsername.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a borrower username.");
                    event.consume();
                    return;
                }

                User targetUser = userDAO.getUserByUsername(borrowerUsername);
                if (targetUser == null) {
                    // Create new user in database
                    User newUser = new User();
                    newUser.setUsername(borrowerUsername);
                    newUser.setPasswordHash(tracker.util.SecurityUtil.hashPassword("password"));
                    newUser.setUserRole("BORROWER");
                    
                    if (userDAO.registerUser(newUser)) {
                        showAlert(Alert.AlertType.INFORMATION, "New Borrower Registered", 
                            "The borrower '" + borrowerUsername + "' did not exist, so a new borrower account has been automatically created with the default password 'password'.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Registration Failed", "Failed to automatically create borrower account.");
                        event.consume();
                        return;
                    }
                }

                Equipment selectedEquip = equipmentBox.getValue();
                if (selectedEquip == null) {
                    String enteredText = equipmentBox.getEditor().getText().trim();
                    selectedEquip = availableEquip.stream()
                        .filter(eq -> {
                            String fullDisplay = eq.getEquipmentName() + " (" + eq.getTechnicalSpecifications() + ")";
                            return fullDisplay.equalsIgnoreCase(enteredText) || 
                                   (eq.getEquipmentName() != null && eq.getEquipmentName().equalsIgnoreCase(enteredText));
                        })
                        .findFirst()
                        .orElse(null);
                }

                if (selectedEquip == null) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select or type a valid available equipment.");
                    event.consume();
                    return;
                }
            });
        });

        dialog.showAndWait().ifPresent(b -> {
            if (bookingDAO.addBooking(b)) {
                // Log the new booking as a change
                List<Booking> allBookings = bookingDAO.getAllBookings();
                Booking newest = allBookings.isEmpty() ? null : allBookings.get(allBookings.size() - 1);
                if (newest != null) {
                    String equipName = equipmentDAO.getEquipmentById(b.getEquipmentId()) != null
                        ? equipmentDAO.getEquipmentById(b.getEquipmentId()).getEquipmentName()
                        : "Equipment #" + b.getEquipmentId();
                    changeLogDAO.logChange("Booking", newest.getBookingId(),
                        "Booking #" + newest.getBookingId(),
                        "New Booking Added", "—",
                        "Equipment: " + equipName + ", Status: " + b.getBookingStatus(),
                        currentUser.getUserId());
                }
                showLoanApprovals();
            }
        });
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.initOwner(mainApp.getPrimaryStage());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle(
            "-fx-background-color: #1a1a2e; -fx-border-color: #2a2a45;"
        );
        alert.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════
    //  MAINTENANCE PANEL
    // ═══════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void showMaintenance() {
        headerTitle.setText("Maintenance Logs");
        contentArea.getChildren().clear();

        VBox panel = new VBox(20);
        panel.setPadding(new Insets(0));

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.getStyleClass().add("action-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Track equipment maintenance and repair records");
        subtitle.getStyleClass().add("content-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Log Maintenance");
        // Use violet primary for maintenance add button as requested
        addBtn.getStyleClass().addAll("button", "btn-primary");
        addBtn.setId("add-maintenance");

        Button exportBtn = new Button("\u2B07 Export");
        exportBtn.getStyleClass().addAll("button", "btn-secondary");
        exportBtn.setOnAction(e -> showExportDialog());

        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.getItems().addAll("Sort: Recent First", "Sort: Oldest First", "Sort: Cost");
        sortBox.setValue("Sort: Recent First");
        sortBox.getStyleClass().add("form-select");
        sortBox.setStyle("-fx-pref-width: 160px;");

        toolbar.getChildren().addAll(subtitle, sortBox, spacer, addBtn, exportBtn);

        // Table
        TableView<MaintenanceLog> table = new TableView<>();
        table.setId("maintenance-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getStyleClass().add("modern-table");
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setMinHeight(500);

        TableColumn<MaintenanceLog, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getLogId()).asObject());
        colId.setMaxWidth(70);
        colId.setVisible(false);

        TableColumn<MaintenanceLog, String> colEquip = new TableColumn<>("Equipment Name");
        colEquip.setCellValueFactory(c -> {
            Equipment eq = equipmentDAO.getEquipmentById(c.getValue().getEquipmentId());
            return new SimpleStringProperty(eq != null ? eq.getEquipmentName() : "Unknown");
        });

        TableColumn<MaintenanceLog, String> colDefect = new TableColumn<>("Defect Description");
        colDefect.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDefectDescription()));
        colDefect.setMinWidth(250);

        TableColumn<MaintenanceLog, Double> colCost = new TableColumn<>("Parts Cost");
        colCost.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPartsCost()).asObject());
        colCost.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("\u20B1%.2f", item));
            }
        });

        TableColumn<MaintenanceLog, String> colTech = new TableColumn<>("Technician");
        colTech.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTechnicianDetails()));

        TableColumn<MaintenanceLog, String> colStart = new TableColumn<>("Start Date");
        colStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStartDate()));

        TableColumn<MaintenanceLog, String> colEnd = new TableColumn<>("Completion Date");
        colEnd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCompletionDate()));
        colEnd.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    MaintenanceLog mLog = getTableView().getItems().get(getIndex());
                    if (item == null || item.trim().isEmpty() || "IN_PROGRESS".equals(mLog.getRepairStatus())) {
                        Button btn = new Button("Returned");
                        btn.getStyleClass().addAll("button", "btn-primary");
                        btn.setStyle("-fx-padding: 4 12; -fx-font-size: 12px;");
                        btn.setOnAction(e -> {
                            String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                            mLog.setCompletionDate(now);
                            mLog.setRepairStatus("COMPLETED");
                            
                            maintenanceDAO.updateMaintenanceLog(mLog);
                            
                            Equipment eq = equipmentDAO.getEquipmentById(mLog.getEquipmentId());
                            if (eq != null) {
                                eq.setEquipmentStatus("AVAILABLE");
                                equipmentDAO.updateEquipment(eq);
                            }
                            
                            changeLogDAO.logChange("Maintenance", mLog.getLogId(), eq != null ? eq.getEquipmentName() : "Unknown",
                                "Maintenance Completed", "—", "Returned on " + now, currentUser.getUserId());
                            
                            showMaintenance();
                        });
                        setGraphic(btn);
                        setText(null);
                    } else {
                        setGraphic(null);
                        setText(item);
                    }
                }
            }
        });

        TableColumn<MaintenanceLog, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRepairStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    if ("COMPLETED".equals(item.toUpperCase())) {
                        badge.getStyleClass().add("badge-completed");
                    } else {
                        badge.getStyleClass().add("badge-in-progress");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        table.getColumns().addAll(colId, colEquip, colDefect, colCost, colTech, colStart, colEnd, colStatus);

        ObservableList<MaintenanceLog> data = FXCollections.observableArrayList(maintenanceDAO.getAllMaintenanceLogs());
        sortBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Sort: Recent First".equals(newVal)) {
                FXCollections.sort(data, java.util.Comparator.comparing((MaintenanceLog m) -> m.getStartDate() == null ? "" : m.getStartDate()).reversed());
            } else if ("Sort: Oldest First".equals(newVal)) {
                FXCollections.sort(data, java.util.Comparator.comparing(m -> m.getStartDate() == null ? "" : m.getStartDate()));
            } else if ("Sort: Cost".equals(newVal)) {
                FXCollections.sort(data, java.util.Comparator.comparing(MaintenanceLog::getPartsCost).reversed());
            }
        });
        FXCollections.sort(data, java.util.Comparator.comparing((MaintenanceLog m) -> m.getStartDate() == null ? "" : m.getStartDate()).reversed());
        table.setItems(data);

        // Add maintenance dialog
        addBtn.setOnAction(e -> showAddMaintenanceDialog(data));

        panel.getChildren().addAll(toolbar, table);
        contentArea.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_LEFT);
    }

    private void showAddMaintenanceDialog(ObservableList<MaintenanceLog> data) {
        Dialog<MaintenanceLog> dialog = new Dialog<>();
        dialog.setTitle("Log Maintenance");
        dialog.initOwner(mainApp.getPrimaryStage());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #1a1a2e; -fx-border-color: #2a2a45; -fx-border-radius: 12; -fx-background-radius: 12;"
        );
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(24));

        ComboBox<Equipment> equipmentBox = new ComboBox<>();
        equipmentBox.setEditable(true);
        equipmentBox.setPromptText("Type to search equipment...");
        equipmentBox.setMaxWidth(Double.MAX_VALUE);
        equipmentBox.getStyleClass().add("form-select");
        
        List<Equipment> allEquip = equipmentDAO.getAllEquipment();
        ObservableList<Equipment> originalList = FXCollections.observableArrayList(allEquip);
        equipmentBox.setItems(originalList);
        
        equipmentBox.setConverter(new javafx.util.StringConverter<Equipment>() {
            @Override
            public String toString(Equipment eq) {
                return eq == null ? "" : (eq.getEquipmentName() + " (" + eq.getTechnicalSpecifications() + ")");
            }

            @Override
            public Equipment fromString(String string) {
                if (string == null || string.trim().isEmpty()) return null;
                String trimmed = string.trim();
                return allEquip.stream()
                    .filter(eq -> {
                        String fullDisplay = eq.getEquipmentName() + " (" + eq.getTechnicalSpecifications() + ")";
                        return fullDisplay.equalsIgnoreCase(trimmed) || 
                               (eq.getEquipmentName() != null && eq.getEquipmentName().equalsIgnoreCase(trimmed));
                    })
                    .findFirst()
                    .orElse(null);
            }
        });

        equipmentBox.setCellFactory(col -> new ListCell<>() {
            @Override
            protected void updateItem(Equipment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getEquipmentName() + " (" + item.getTechnicalSpecifications() + ")");
                }
            }
        });

        equipmentBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                equipmentBox.setItems(originalList);
                equipmentBox.hide();
            } else {
                Equipment selected = equipmentBox.getSelectionModel().getSelectedItem();
                String trimmed = newVal.trim();
                if (selected != null) {
                    String fullDisplay = selected.getEquipmentName() + " (" + selected.getTechnicalSpecifications() + ")";
                    if (fullDisplay.equalsIgnoreCase(trimmed) || 
                        (selected.getEquipmentName() != null && selected.getEquipmentName().equalsIgnoreCase(trimmed))) {
                        return;
                    }
                }
                ObservableList<Equipment> filteredList = FXCollections.observableArrayList();
                for (Equipment eq : originalList) {
                    if ((eq.getEquipmentName() != null && eq.getEquipmentName().toLowerCase().contains(newVal.toLowerCase())) ||
                        (eq.getSerialNumber() != null && eq.getSerialNumber().toLowerCase().contains(newVal.toLowerCase())) ||
                        (eq.getTechnicalSpecifications() != null && eq.getTechnicalSpecifications().toLowerCase().contains(newVal.toLowerCase()))) {
                        filteredList.add(eq);
                    }
                }
                equipmentBox.setItems(filteredList);
                if (!filteredList.isEmpty()) equipmentBox.show();
                else equipmentBox.hide();
            }
        });

        TextArea defectField = new TextArea();
        defectField.setPromptText("Describe the defect...");
        defectField.setPrefRowCount(3);
        defectField.getStyleClass().add("form-textarea");

        TextField costField = new TextField();
        costField.setPromptText("Parts Cost");
        costField.getStyleClass().add("form-input");

        TextField techField = new TextField();
        techField.setPromptText("Technician Name/Details");
        techField.getStyleClass().add("form-input");

        TextField dateField = new TextField();
        dateField.setPromptText("YYYY-MM-DD");
        dateField.getStyleClass().add("form-input");

        Label l1 = new Label("Equipment Name"); l1.getStyleClass().add("form-label");
        Label l2 = new Label("Defect");       l2.getStyleClass().add("form-label");
        Label l3 = new Label("Parts Cost");   l3.getStyleClass().add("form-label");
        Label l4 = new Label("Technician");   l4.getStyleClass().add("form-label");
        Label l5 = new Label("Start Date");   l5.getStyleClass().add("form-label");

        grid.addRow(0, l1, equipmentBox);
        grid.addRow(1, l2, defectField);
        grid.addRow(2, l3, costField);
        grid.addRow(3, l4, techField);
        grid.addRow(4, l5, dateField);

        dialogPane.setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                MaintenanceLog log = new MaintenanceLog();
                Equipment selectedEquip = equipmentBox.getValue();
                if (selectedEquip == null) {
                    String enteredText = equipmentBox.getEditor().getText().trim();
                    selectedEquip = allEquip.stream()
                        .filter(eq -> {
                            String fullDisplay = eq.getEquipmentName() + " (" + eq.getTechnicalSpecifications() + ")";
                            return fullDisplay.equalsIgnoreCase(enteredText) || 
                                   (eq.getEquipmentName() != null && eq.getEquipmentName().equalsIgnoreCase(enteredText));
                        })
                        .findFirst()
                        .orElse(null);
                }
                
                if (selectedEquip != null) {
                    log.setEquipmentId(selectedEquip.getEquipmentId());
                } else {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a valid equipment.");
                    return null;
                }
                log.setDefectDescription(defectField.getText());
                try {
                    log.setPartsCost(Double.parseDouble(costField.getText()));
                } catch (NumberFormatException ex) {
                    log.setPartsCost(0);
                }
                log.setTechnicianDetails(techField.getText());
                log.setStartDate(dateField.getText());
                log.setRepairStatus("IN_PROGRESS");
                return log;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(log -> {
            if (maintenanceDAO.addMaintenanceLog(log)) {
                data.setAll(maintenanceDAO.getAllMaintenanceLogs());
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  LOGS PANEL  (with Inventory / Booking sub-header tabs)
    // ═══════════════════════════════════════════════════════════

    private Button activeLogTabBtn; // tracks the active sub-header button

    private void showAuditLogs() {
        headerTitle.setText("Logs");
        contentArea.getChildren().clear();

        VBox panel = new VBox(0);
        panel.setPadding(new Insets(0));

        // ── Toolbar row ──
        HBox toolbar = new HBox(12);
        toolbar.getStyleClass().add("action-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Track every change made to your data");
        subtitle.getStyleClass().add("content-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("\uD83D\uDCE4 Export");
        exportBtn.getStyleClass().addAll("button", "btn-primary");
        exportBtn.setId("export-data");
        exportBtn.setOnAction(e -> showExportDialog());

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.getStyleClass().add("button");
        refreshBtn.setOnAction(e -> showAuditLogs());

        toolbar.getChildren().addAll(subtitle, spacer, exportBtn, refreshBtn);

        // ── Sub-header tab bar (Inventory | Booking) ──
        HBox tabBar = new HBox(8);
        tabBar.getStyleClass().add("log-tab-bar");
        tabBar.setAlignment(Pos.CENTER_LEFT);

        // Container for the table that changes based on selected tab
        VBox tableContainer = new VBox();
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        Button tabInventory = new Button("📦  Inventory");
        tabInventory.getStyleClass().add("log-tab-btn");
        tabInventory.setId("log-tab-inventory");

        Button tabBooking = new Button("📄  Booking");
        tabBooking.getStyleClass().add("log-tab-btn");
        tabBooking.setId("log-tab-booking");

        tabInventory.setOnAction(e -> {
            setActiveLogTab(tabInventory);
            showChangeLogTable(tableContainer, "Inventory");
        });

        tabBooking.setOnAction(e -> {
            setActiveLogTab(tabBooking);
            showChangeLogTable(tableContainer, "Booking");
        });

        tabBar.getChildren().addAll(tabInventory, tabBooking);

        // Default: select Inventory tab
        setActiveLogTab(tabInventory);

        panel.getChildren().addAll(toolbar, tabBar, tableContainer);
        contentArea.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_LEFT);

        // Show Inventory logs by default
        showChangeLogTable(tableContainer, "Inventory");
    }

    /**
     * Highlights the active sub-header tab button.
     */
    private void setActiveLogTab(Button btn) {
        if (activeLogTabBtn != null) {
            activeLogTabBtn.getStyleClass().remove("log-tab-btn-active");
        }
        btn.getStyleClass().add("log-tab-btn-active");
        activeLogTabBtn = btn;
    }

    /**
     * Populates the table container with change-log rows for the given table name.
     */
    @SuppressWarnings("unchecked")
    private void showChangeLogTable(VBox container, String tableName) {
        container.getChildren().clear();

        // Fetch change logs
        List<ChangeLog> logs = changeLogDAO.getChangeLogsByTable(tableName);

        // Search / filter bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(16, 0, 12, 0));

        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search changes...");
        searchField.getStyleClass().add("search-field");
        searchField.setMinWidth(300);

        Label countLabel = new Label(logs.size() + " change" + (logs.size() != 1 ? "s" : "") + " recorded");
        countLabel.setStyle("-fx-text-fill: -text-muted; -fx-font-size: 13px;");

        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);

        filterBar.getChildren().addAll(searchField, filterSpacer, countLabel);

        // Table
        TableView<ChangeLog> table = new TableView<>();
        table.setId("changelog-table-" + tableName.toLowerCase());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getStyleClass().add("modern-table");
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setMinHeight(450);

        TableColumn<ChangeLog, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRecordName()));
        colName.setMinWidth(160);

        TableColumn<ChangeLog, String> colField = new TableColumn<>("Field Changed");
        colField.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFieldName()));
        colField.setMinWidth(140);

        TableColumn<ChangeLog, String> colEdited = new TableColumn<>("Edited Into");
        colEdited.setCellValueFactory(c -> {
            String oldVal = c.getValue().getOldValue();
            String newVal = c.getValue().getNewValue();
            String display = (oldVal != null && !oldVal.isEmpty() && !"—".equals(oldVal))
                ? oldVal + "  →  " + newVal
                : newVal;
            return new SimpleStringProperty(display);
        });
        colEdited.setMinWidth(250);
        colEdited.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    if (item.contains("→")) {
                        lbl.setStyle("-fx-text-fill: #ffd166; -fx-font-size: 13px;");
                    } else {
                        lbl.setStyle("-fx-text-fill: #06d6a0; -fx-font-size: 13px;");
                    }
                    lbl.setWrapText(true);
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        TableColumn<ChangeLog, String> colTime = new TableColumn<>("Time Changed");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getChangeTimestamp()));
        colTime.setMinWidth(180);

        TableColumn<ChangeLog, String> colUser = new TableColumn<>("Changed By");
        colUser.setCellValueFactory(c -> {
            String username = c.getValue().getUsername();
            return new SimpleStringProperty(username != null ? username : "User #" + c.getValue().getUserId());
        });
        colUser.setMinWidth(130);
        colUser.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle(
                        "-fx-background-color: rgba(67,97,238,0.12); -fx-text-fill: #7b93f5; " +
                        "-fx-padding: 4 12; -fx-background-radius: 14; -fx-font-size: 12px; -fx-font-weight: bold;"
                    );
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        table.getColumns().addAll(colName, colField, colEdited, colTime, colUser);

        ObservableList<ChangeLog> data = FXCollections.observableArrayList(logs);
        FilteredList<ChangeLog> filteredData = new FilteredList<>(data, p -> true);
        table.setItems(filteredData);

        // Empty state placeholder
        Label placeholder = new Label("No changes recorded for " + tableName + " yet.");
        placeholder.setStyle("-fx-text-fill: -text-muted; -fx-font-size: 14px;");
        table.setPlaceholder(placeholder);

        // Search filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(cl -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return (cl.getRecordName() != null && cl.getRecordName().toLowerCase().contains(lower))
                    || (cl.getFieldName() != null && cl.getFieldName().toLowerCase().contains(lower))
                    || (cl.getNewValue() != null && cl.getNewValue().toLowerCase().contains(lower))
                    || (cl.getOldValue() != null && cl.getOldValue().toLowerCase().contains(lower))
                    || (cl.getUsername() != null && cl.getUsername().toLowerCase().contains(lower));
            });
        });

        container.getChildren().addAll(filterBar, table);
    }

    // ═══════════════════════════════════════════════════════════
    //  EXPORT PANEL
    // ═══════════════════════════════════════════════════════════

    private void showExportDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Export Data");
        dialog.initOwner(mainApp.getPrimaryStage());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #1a1a2e; -fx-border-color: #2a2a45; -fx-border-radius: 16; -fx-background-radius: 16;"
        );
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // ── Dialog Content ──
        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 32, 12, 32));
        content.getStyleClass().add("modal-card");

        Label titleLbl = new Label("Select Data to Export");
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e8e8f0;");

        Label descLbl = new Label("Choose a data source to export as a CSV file.");
        descLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #8888a8;");

        // Option cards in a VBox
        ToggleGroup group = new ToggleGroup();

        RadioButton optInventory  = buildExportOption(group, "📦", "Inventory",  "All equipment, status, specs, location and cost");
        RadioButton optRecords    = buildExportOption(group, "📄", "Records",    "All borrowing booking records with status");
        RadioButton optAuditLogs  = buildExportOption(group, "📋", "Logs", "System activity: inserts, updates and deletes");

        optInventory.setSelected(true);

        VBox optionsBox = new VBox(10, optInventory, optRecords, optAuditLogs);

        content.getChildren().addAll(titleLbl, descLbl, optionsBox);
        dialogPane.setContent(content);

        // Style OK button: use primary (violet) class instead of inline blue gradient
        Button dlgOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (dlgOk != null) {
            dlgOk.getStyleClass().addAll("button", "btn-primary");
            dlgOk.setStyle("-fx-padding: 10 28; -fx-font-weight: bold;");
        }

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                RadioButton selected = (RadioButton) group.getSelectedToggle();
                return selected != null ? selected.getUserData().toString() : null;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(choice -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save " + choice + " Export");
            fileChooser.setInitialFileName(choice.toLowerCase().replace(" ", "_") + "_export.csv");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv")
            );
            File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());
            if (file != null) {
                exportToCSV(choice, file);
            }
        });
    }

    private RadioButton buildExportOption(ToggleGroup group, String icon, String label, String desc) {
        RadioButton rb = new RadioButton();
        rb.setToggleGroup(group);
        rb.setUserData(label);
        rb.setMaxWidth(Double.MAX_VALUE);

        VBox textBox = new VBox(2);
        Label titleLbl = new Label(icon + "  " + label);
        // use theme tokens so colors match current theme
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -text-primary;");
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-secondary;");
        textBox.getChildren().addAll(titleLbl, descLbl);

        // Use the RadioButton's graphic to render a full-width card row.
        rb.setGraphic(textBox);
        rb.getStyleClass().add("export-option");
        rb.setCursor(javafx.scene.Cursor.HAND);
        rb.setOnMouseClicked(e -> rb.setSelected(true));

        // Toggle a selected CSS class on the RadioButton so the stylesheet can update the
        // visual card state (keeps styling centralized in CSS rather than inline styles).
        rb.selectedProperty().addListener((obs, oldVal, selected) -> {
            if (selected) {
                if (!rb.getStyleClass().contains("export-option-selected"))
                    rb.getStyleClass().add("export-option-selected");
            } else {
                rb.getStyleClass().remove("export-option-selected");
            }
        });

        return rb;
    }

    private void exportToCSV(String dataType, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            switch (dataType) {
                case "Inventory" -> {
                    writer.write("Equipment ID,Serial Number,Category ID,Specifications,Location,Purchase Cost,Purchase Date,Status,Assigned To");
                    writer.newLine();
                    for (Equipment eq : equipmentDAO.getAllEquipment()) {
                        writer.write(String.join(",",
                            String.valueOf(eq.getEquipmentId()),
                            csv(eq.getSerialNumber()),
                            String.valueOf(eq.getCategoryId()),
                            csv(eq.getTechnicalSpecifications()),
                            csv(eq.getStorageLocation()),
                            String.format("%.2f", eq.getPurchaseCost()),
                            csv(eq.getPurchaseDate()),
                            csv(eq.getEquipmentStatus()),
                            eq.getAssignedTo() != null ? String.valueOf(eq.getAssignedTo()) : ""
                        ));
                        writer.newLine();
                    }
                }
                case "Records" -> {
                    writer.write("Booking ID,Equipment ID,Borrower ID,Start Date,Expected Return,Purpose,Status");
                    writer.newLine();
                    for (Booking b : bookingDAO.getAllBookings()) {
                        writer.write(String.join(",",
                            String.valueOf(b.getBookingId()),
                            String.valueOf(b.getEquipmentId()),
                            String.valueOf(b.getBorrowerId()),
                            csv(b.getStartDatetime()),
                            csv(b.getExpectedReturnDatetime()),
                            csv(b.getPurposeDescription()),
                            csv(b.getBookingStatus())
                        ));
                        writer.newLine();
                    }
                }
                case "Logs" -> {
                    writer.write("Audit ID,Action Type,Affected Table,Record ID,Timestamp,User ID");
                    writer.newLine();
                    for (AuditLog log : auditDAO.getAllAuditLogs()) {
                        writer.write(String.join(",",
                            String.valueOf(log.getAuditId()),
                            csv(log.getActionType()),
                            csv(log.getAffectedTable()),
                            String.valueOf(log.getRecordId()),
                            csv(log.getActionTimestamp()),
                            String.valueOf(log.getUserId())
                        ));
                        writer.newLine();
                    }
                }
            }
            showExportSuccess(file);
        } catch (IOException ex) {
            showExportError(ex.getMessage());
        }
    }

    /** Escapes a value for CSV: wraps in quotes if it contains commas/quotes/newlines. */
    private String csv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showExportSuccess(File file) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(mainApp.getPrimaryStage());
        alert.setTitle("Export Successful");
        alert.setHeaderText(null);
        alert.setContentText("✅ Data exported successfully to:\n" + file.getAbsolutePath());
        alert.getDialogPane().setStyle(
            "-fx-background-color: #1a1a2e; -fx-border-color: #2a2a45;"
        );
        alert.showAndWait();
    }

    private void showExportError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(mainApp.getPrimaryStage());
        alert.setTitle("Export Failed");
        alert.setHeaderText(null);
        alert.setContentText("❌ Export failed: " + message);
        alert.getDialogPane().setStyle(
            "-fx-background-color: #1a1a2e; -fx-border-color: #2a2a45;"
        );
        alert.showAndWait();
    }

    private void showSettings() {
        headerTitle.setText("Settings");
        contentArea.getChildren().clear();
        
        Runnable onThemeChanged = () -> {
            if ("DARK".equalsIgnoreCase(currentUser.getThemePreference())) {
                isLightMode = false;
                if (!rootView.getScene().getRoot().getStyleClass().contains("dark-theme")) {
                    rootView.getScene().getRoot().getStyleClass().add("dark-theme");
                }
            } else {
                isLightMode = true;
                rootView.getScene().getRoot().getStyleClass().remove("dark-theme");
            }
        };

        SettingsScreen settingsScreen = new SettingsScreen(mainApp, currentUser, onThemeChanged);
        VBox panel = settingsScreen.getView();
        contentArea.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_LEFT);
    }

}
package tracker.ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private BorderPane view;
    private StackPane contentArea;
    private VBox sidebarBtnContainer;
    private Button activeSidebarBtn;
    private Label headerTitle;

    // DAOs
    private EquipmentDAO equipmentDAO = new EquipmentDAO();
    private BookingDAO bookingDAO = new BookingDAO();
    private MaintenanceDAO maintenanceDAO = new MaintenanceDAO();
    private AuditDAO auditDAO = new AuditDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();

    /**
     * Create the admin dashboard.
     */
    public AdminDashboard(Main mainApp, User user) {
        this.mainApp = mainApp;
        this.currentUser = user;
        buildView();
    }

    public BorderPane getView() {
        return view;
    }

    private void buildView() {
        view = new BorderPane();

        // ── Sidebar ──
        VBox sidebar = buildSidebar();
        view.setLeft(sidebar);

        // ── Header ──
        HBox header = buildHeader();
        view.setTop(header);

        // ── Content Area ──
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        view.setCenter(scrollPane);

        // Show dashboard by default
        showDashboard();
    }

    // ═══════════════════════════════════════════════════════════
    //  SIDEBAR
    // ═══════════════════════════════════════════════════════════

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        // Brand header
        VBox brandBox = new VBox(4);
        brandBox.getStyleClass().add("sidebar-header");

        Label brandName = new Label("\u26A1 EAT System");
        brandName.getStyleClass().add("sidebar-brand");

        Label brandSub = new Label("Admin Control Panel");
        brandSub.getStyleClass().add("sidebar-brand-sub");

        brandBox.getChildren().addAll(brandName, brandSub);

        // Section label
        Label navLabel = new Label("NAVIGATION");
        navLabel.getStyleClass().add("sidebar-section-label");

        // Navigation buttons
        sidebarBtnContainer = new VBox(0);

        Button btnDashboard = createSidebarButton("\uD83D\uDCCA  Dashboard", () -> showDashboard());
        Button btnInventory = createSidebarButton("\uD83D\uDCE6  Inventory", () -> showInventory());
        Button btnLoans = createSidebarButton("\uD83D\uDCC4  Records", () -> showLoanApprovals());
        Button btnMaintenance = createSidebarButton("\uD83D\uDD27  Maintenance", () -> showMaintenance());
        Button btnAudit = createSidebarButton("\uD83D\uDCCB  Audit Logs", () -> showAuditLogs());

        sidebarBtnContainer.getChildren().addAll(btnDashboard, btnInventory, btnLoans, btnMaintenance, btnAudit);

        // Set first button active
        setActiveSidebarBtn(btnDashboard);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer
        VBox footer = new VBox(8);
        footer.getStyleClass().add("sidebar-footer");

        Label footerUser = new Label("\u26A1 " + currentUser.getUsername());
        footerUser.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 12px;");

        Label footerRole = new Label("ADMINISTRATOR");
        footerRole.getStyleClass().add("header-role-badge");

        footer.getChildren().addAll(footerUser, footerRole);

        sidebar.getChildren().addAll(brandBox, navLabel, sidebarBtnContainer, spacer, footer);
        return sidebar;
    }

    private Button createSidebarButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            setActiveSidebarBtn(btn);
            action.run();
        });
        return btn;
    }

    private void setActiveSidebarBtn(Button btn) {
        if (activeSidebarBtn != null) {
            activeSidebarBtn.getStyleClass().remove("sidebar-btn-active");
        }
        btn.getStyleClass().add("sidebar-btn-active");
        activeSidebarBtn = btn;
    }

    // ═══════════════════════════════════════════════════════════
    //  HEADER
    // ═══════════════════════════════════════════════════════════

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.getStyleClass().add("header-bar");
        header.setAlignment(Pos.CENTER_LEFT);

        headerTitle = new Label("Inventory Management");
        headerTitle.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("Logged in as  ");
        userLabel.getStyleClass().add("header-user");

        Label userName = new Label(currentUser.getUsername());
        userName.setStyle("-fx-text-fill: -text-primary; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label roleBadge = new Label("ADMIN");
        roleBadge.getStyleClass().add("header-role-badge");

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("btn-logout");
        logoutBtn.setId("admin-logout");
        logoutBtn.setOnAction(e -> mainApp.showLoginScreen());

        header.getChildren().addAll(headerTitle, spacer, userLabel, userName, roleBadge, logoutBtn);
        return header;
    }

    // ═══════════════════════════════════════════════════════════
    //  DASHBOARD PANEL
    // ═══════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void showDashboard() {
        headerTitle.setText("Dashboard Summary");
        contentArea.getChildren().clear();

        VBox panel = new VBox(20);
        panel.setPadding(new Insets(0));

        // 1. Charts Row
        HBox chartsRow = new HBox(20);
        chartsRow.setAlignment(Pos.CENTER_LEFT);

        // -- Pie Chart: Equipment Status --
        VBox pieCard = new VBox(10);
        pieCard.getStyleClass().add("card");
        HBox.setHgrow(pieCard, Priority.ALWAYS);
        
        Label pieTitle = new Label("Equipment Status Distribution");
        pieTitle.getStyleClass().add("card-header");
        
        List<Equipment> allEquipment = equipmentDAO.getAllEquipment();
        long available = allEquipment.stream().filter(e -> "AVAILABLE".equals(e.getEquipmentStatus())).count();
        long borrowed = allEquipment.stream().filter(e -> "BORROWED".equals(e.getEquipmentStatus())).count();
        long inMaint = allEquipment.stream().filter(e -> "IN_MAINTENANCE".equals(e.getEquipmentStatus())).count();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data("Available", available),
            new PieChart.Data("Borrowed", borrowed),
            new PieChart.Data("In Maintenance", inMaint)
        );
        PieChart statusChart = new PieChart(pieData);
        statusChart.setLegendVisible(true);
        statusChart.setLabelsVisible(false);
        statusChart.setMinHeight(300);
        
        pieCard.getChildren().addAll(pieTitle, statusChart);

        // -- Bar Chart: Equipment per Category --
        VBox barCard = new VBox(10);
        barCard.getStyleClass().add("card");
        HBox.setHgrow(barCard, Priority.ALWAYS);

        Label barTitle = new Label("Equipment per Category");
        barTitle.getStyleClass().add("card-header");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);
        
        BarChart<String, Number> categoryChart = new BarChart<>(xAxis, yAxis);
        categoryChart.setLegendVisible(false);
        categoryChart.setMinHeight(300);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        List<Category> categories = categoryDAO.getAllCategories();
        Map<Integer, Long> categoryCounts = allEquipment.stream()
            .collect(Collectors.groupingBy(Equipment::getCategoryId, Collectors.counting()));
            
        for (Category cat : categories) {
            long count = categoryCounts.getOrDefault(cat.getCategoryId(), 0L);
            series.getData().add(new XYChart.Data<>(cat.getCategoryName(), count));
        }
        categoryChart.getData().add(series);
        
        barCard.getChildren().addAll(barTitle, categoryChart);
        chartsRow.getChildren().addAll(pieCard, barCard);

        // 2. Tables Row
        HBox tablesRow = new HBox(20);
        tablesRow.setAlignment(Pos.TOP_LEFT);

        // Fetch Bookings
        List<Booking> allBookings = bookingDAO.getAllBookings();

        // -- Returning Equipment Table (APPROVED / Currently Borrowed) --
        VBox returningCard = new VBox(10);
        returningCard.getStyleClass().add("card");
        HBox.setHgrow(returningCard, Priority.ALWAYS);

        Label returningTitle = new Label("Currently Borrowed (To Be Returned)");
        returningTitle.getStyleClass().add("card-header");

        TableView<Booking> returningTable = new TableView<>();
        returningTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        returningTable.setMinHeight(250);

        TableColumn<Booking, Integer> rColId = new TableColumn<>("Booking ID");
        rColId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getBookingId()).asObject());
        
        TableColumn<Booking, Integer> rColEquip = new TableColumn<>("Equipment ID");
        rColEquip.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getEquipmentId()).asObject());

        TableColumn<Booking, String> rColReturn = new TableColumn<>("Expected Return");
        rColReturn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getExpectedReturnDatetime()));
        
        returningTable.getColumns().addAll(rColId, rColEquip, rColReturn);
        
        List<Booking> borrowedBookings = allBookings.stream()
            .filter(b -> "APPROVED".equals(b.getBookingStatus()))
            .collect(Collectors.toList());
        returningTable.setItems(FXCollections.observableArrayList(borrowedBookings));
        
        returningCard.getChildren().addAll(returningTitle, returningTable);

        // -- Upcoming Schedule Table (PENDING) --
        VBox upcomingCard = new VBox(10);
        upcomingCard.getStyleClass().add("card");
        HBox.setHgrow(upcomingCard, Priority.ALWAYS);

        Label upcomingTitle = new Label("Upcoming Schedule (Pending Requests)");
        upcomingTitle.getStyleClass().add("card-header");

        TableView<Booking> upcomingTable = new TableView<>();
        upcomingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        upcomingTable.setMinHeight(250);

        TableColumn<Booking, Integer> uColId = new TableColumn<>("Booking ID");
        uColId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getBookingId()).asObject());
        
        TableColumn<Booking, Integer> uColEquip = new TableColumn<>("Equipment ID");
        uColEquip.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getEquipmentId()).asObject());

        TableColumn<Booking, String> uColStart = new TableColumn<>("Start Date");
        uColStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStartDatetime()));

        upcomingTable.getColumns().addAll(uColId, uColEquip, uColStart);
        
        List<Booking> pendingBookings = allBookings.stream()
            .filter(b -> "PENDING".equals(b.getBookingStatus()))
            .collect(Collectors.toList());
        upcomingTable.setItems(FXCollections.observableArrayList(pendingBookings));
        
        upcomingCard.getChildren().addAll(upcomingTitle, upcomingTable);

        tablesRow.getChildren().addAll(returningCard, upcomingCard);

        panel.getChildren().addAll(chartsRow, tablesRow);
        contentArea.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_LEFT);
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
        addBtn.getStyleClass().addAll("button", "btn-success");
        addBtn.setId("add-equipment");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("\u21BB Refresh");
        refreshBtn.getStyleClass().add("button");
        refreshBtn.setOnAction(e -> showInventory());

        toolbar.getChildren().addAll(searchField, spacer, addBtn, refreshBtn);

        // Table
        TableView<Equipment> table = new TableView<>();
        table.setId("equipment-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setMinHeight(500);

        TableColumn<Equipment, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getEquipmentId()).asObject());
        colId.setMaxWidth(70);
        colId.setMinWidth(70);

        TableColumn<Equipment, String> colSerial = new TableColumn<>("Serial Number");
        colSerial.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSerialNumber()));

        TableColumn<Equipment, Integer> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCategoryId()).asObject());
        colCategory.setMaxWidth(100);

        TableColumn<Equipment, String> colSpecs = new TableColumn<>("Specifications");
        colSpecs.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTechnicalSpecifications()));
        colSpecs.setMinWidth(200);

        TableColumn<Equipment, String> colLocation = new TableColumn<>("Location");
        colLocation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStorageLocation()));

        TableColumn<Equipment, Double> colCost = new TableColumn<>("Cost");
        colCost.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPurchaseCost()).asObject());
        colCost.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("$%.2f", item));
            }
        });

        TableColumn<Equipment, String> colDate = new TableColumn<>("Purchase Date");
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPurchaseDate()));

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

        table.getColumns().addAll(colId, colSerial, colCategory, colSpecs, colLocation, colCost, colDate, colStatus);

        // Load data
        ObservableList<Equipment> data = FXCollections.observableArrayList(equipmentDAO.getAllEquipment());
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

    private HBox buildInventoryStats() {
        List<Equipment> allEquipment = equipmentDAO.getAllEquipment();
        long total = allEquipment.size();
        long available = allEquipment.stream().filter(e -> "AVAILABLE".equals(e.getEquipmentStatus())).count();
        long borrowed = allEquipment.stream().filter(e -> "BORROWED".equals(e.getEquipmentStatus())).count();
        long inMaint = allEquipment.stream().filter(e -> "IN_MAINTENANCE".equals(e.getEquipmentStatus())).count();

        HBox statsBar = new HBox(20);
        statsBar.setPadding(new Insets(0, 0, 10, 0));

        statsBar.getChildren().addAll(
            buildStatCard(String.valueOf(total), "Total Equipment", "#4361ee"),
            buildStatCard(String.valueOf(available), "Available", "#06d6a0"),
            buildStatCard(String.valueOf(borrowed), "Borrowed", "#ffd166"),
            buildStatCard(String.valueOf(inMaint), "In Maintenance", "#ef476f")
        );
        return statsBar;
    }

    private VBox buildStatCard(String value, String label, String color) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label valLabel = new Label(value);
        valLabel.getStyleClass().add("stat-value");
        valLabel.setStyle("-fx-text-fill: " + color + ";");

        Label descLabel = new Label(label);
        descLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(valLabel, descLabel);
        return card;
    }

    private void showAddEquipmentDialog(ObservableList<Equipment> data) {
        Dialog<Equipment> dialog = new Dialog<>();
        dialog.setTitle("Add New Equipment");
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

        TextField serialField = new TextField();
        serialField.setPromptText("Serial Number");

        ComboBox<Category> categoryBox = new ComboBox<>();
        categoryBox.setItems(FXCollections.observableArrayList(categoryDAO.getAllCategories()));
        categoryBox.setPromptText("Select Category");

        TextField specsField = new TextField();
        specsField.setPromptText("Technical Specifications");

        TextField locationField = new TextField();
        locationField.setPromptText("Storage Location");

        TextField costField = new TextField();
        costField.setPromptText("Purchase Cost");

        TextField dateField = new TextField();
        dateField.setPromptText("YYYY-MM-DD");

        TextField assignedToField = new TextField();
        assignedToField.setPromptText("User ID (Optional)");

        Label lbl1 = new Label("Serial Number");  lbl1.getStyleClass().add("form-label");
        Label lbl2 = new Label("Category");        lbl2.getStyleClass().add("form-label");
        Label lbl3 = new Label("Specifications");  lbl3.getStyleClass().add("form-label");
        Label lbl4 = new Label("Location");        lbl4.getStyleClass().add("form-label");
        Label lbl5 = new Label("Purchase Cost");   lbl5.getStyleClass().add("form-label");
        Label lbl6 = new Label("Purchase Date");   lbl6.getStyleClass().add("form-label");
        Label lbl7 = new Label("Assigned To");     lbl7.getStyleClass().add("form-label");

        grid.addRow(0, lbl1, serialField);
        grid.addRow(1, lbl2, categoryBox);
        grid.addRow(2, lbl3, specsField);
        grid.addRow(3, lbl4, locationField);
        grid.addRow(4, lbl5, costField);
        grid.addRow(5, lbl6, dateField);
        grid.addRow(6, lbl7, assignedToField);

        dialogPane.setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Equipment eq = new Equipment();
                eq.setSerialNumber(serialField.getText());
                eq.setCategoryId(categoryBox.getValue() != null ? categoryBox.getValue().getCategoryId() : 0);
                eq.setTechnicalSpecifications(specsField.getText());
                eq.setStorageLocation(locationField.getText());
                try {
                    eq.setPurchaseCost(Double.parseDouble(costField.getText()));
                } catch (NumberFormatException ex) {
                    eq.setPurchaseCost(0);
                }
                eq.setPurchaseDate(dateField.getText());
                eq.setEquipmentStatus("AVAILABLE");
                
                String assignedTxt = assignedToField.getText().trim();
                if (!assignedTxt.isEmpty()) {
                    try {
                        eq.setAssignedTo(Integer.parseInt(assignedTxt));
                    } catch (NumberFormatException ex) {
                        eq.setAssignedTo(null);
                    }
                }
                
                return eq;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(eq -> {
            if (equipmentDAO.addEquipment(eq)) {
                data.setAll(equipmentDAO.getAllEquipment());
                showInventory(); // Refresh stats too
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  LOAN APPROVALS PANEL
    // ═══════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void showLoanApprovals() {
        headerTitle.setText("Records");
        contentArea.getChildren().clear();

        VBox panel = new VBox(20);
        panel.setPadding(new Insets(0));

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.getStyleClass().add("action-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Review and manage equipment booking records");
        subtitle.getStyleClass().add("content-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("\u21BB Refresh");
        refreshBtn.getStyleClass().add("button");
        refreshBtn.setOnAction(e -> showLoanApprovals());

        toolbar.getChildren().addAll(subtitle, spacer, refreshBtn);

        // Table
        TableView<Booking> table = new TableView<>();
        table.setId("bookings-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setMinHeight(500);

        TableColumn<Booking, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getBookingId()).asObject());
        colId.setMaxWidth(70);

        TableColumn<Booking, Integer> colEquip = new TableColumn<>("Equipment ID");
        colEquip.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getEquipmentId()).asObject());

        TableColumn<Booking, Integer> colBorrower = new TableColumn<>("Borrower ID");
        colBorrower.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getBorrowerId()).asObject());

        TableColumn<Booking, String> colStart = new TableColumn<>("Start Date");
        colStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStartDatetime()));

        TableColumn<Booking, String> colReturn = new TableColumn<>("Expected Return");
        colReturn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getExpectedReturnDatetime()));

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
                        case "APPROVED":  badge.getStyleClass().add("badge-approved"); break;
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

            {
                approveBtn.getStyleClass().addAll("button", "btn-success");
                approveBtn.setStyle("-fx-padding: 6 16; -fx-font-size: 12px;");
                rejectBtn.getStyleClass().addAll("button", "btn-danger");
                rejectBtn.setStyle("-fx-padding: 6 16; -fx-font-size: 12px;");
                actionBox.setAlignment(Pos.CENTER);
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
                            showLoanApprovals();
                        });
                        rejectBtn.setOnAction(e -> {
                            bookingDAO.updateBookingStatus(booking.getBookingId(), "REJECTED", currentUser.getUserId(), "Rejected by admin");
                            showLoanApprovals();
                        });
                        setGraphic(actionBox);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        table.getColumns().addAll(colId, colEquip, colBorrower, colStart, colReturn, colPurpose, colStatus, colActions);
        table.setItems(FXCollections.observableArrayList(bookingDAO.getAllBookings()));

        panel.getChildren().addAll(toolbar, table);
        contentArea.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_LEFT);
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
        addBtn.getStyleClass().addAll("button", "btn-warning");
        addBtn.setId("add-maintenance");

        Button refreshBtn = new Button("\u21BB Refresh");
        refreshBtn.getStyleClass().add("button");
        refreshBtn.setOnAction(e -> showMaintenance());

        toolbar.getChildren().addAll(subtitle, spacer, addBtn, refreshBtn);

        // Table
        TableView<MaintenanceLog> table = new TableView<>();
        table.setId("maintenance-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setMinHeight(500);

        TableColumn<MaintenanceLog, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getLogId()).asObject());
        colId.setMaxWidth(70);

        TableColumn<MaintenanceLog, Integer> colEquip = new TableColumn<>("Equipment ID");
        colEquip.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getEquipmentId()).asObject());

        TableColumn<MaintenanceLog, String> colDefect = new TableColumn<>("Defect Description");
        colDefect.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDefectDescription()));
        colDefect.setMinWidth(250);

        TableColumn<MaintenanceLog, Double> colCost = new TableColumn<>("Parts Cost");
        colCost.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPartsCost()).asObject());
        colCost.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("$%.2f", item));
            }
        });

        TableColumn<MaintenanceLog, String> colTech = new TableColumn<>("Technician");
        colTech.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTechnicianDetails()));

        TableColumn<MaintenanceLog, String> colStart = new TableColumn<>("Start Date");
        colStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStartDate()));

        TableColumn<MaintenanceLog, String> colEnd = new TableColumn<>("Completion Date");
        colEnd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCompletionDate()));

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

        TextField equipIdField = new TextField();
        equipIdField.setPromptText("Equipment ID");

        TextArea defectField = new TextArea();
        defectField.setPromptText("Describe the defect...");
        defectField.setPrefRowCount(3);

        TextField costField = new TextField();
        costField.setPromptText("Parts Cost");

        TextField techField = new TextField();
        techField.setPromptText("Technician Name/Details");

        TextField dateField = new TextField();
        dateField.setPromptText("YYYY-MM-DD");

        Label l1 = new Label("Equipment ID"); l1.getStyleClass().add("form-label");
        Label l2 = new Label("Defect");       l2.getStyleClass().add("form-label");
        Label l3 = new Label("Parts Cost");   l3.getStyleClass().add("form-label");
        Label l4 = new Label("Technician");   l4.getStyleClass().add("form-label");
        Label l5 = new Label("Start Date");   l5.getStyleClass().add("form-label");

        grid.addRow(0, l1, equipIdField);
        grid.addRow(1, l2, defectField);
        grid.addRow(2, l3, costField);
        grid.addRow(3, l4, techField);
        grid.addRow(4, l5, dateField);

        dialogPane.setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                MaintenanceLog log = new MaintenanceLog();
                try {
                    log.setEquipmentId(Integer.parseInt(equipIdField.getText()));
                } catch (NumberFormatException ex) {
                    log.setEquipmentId(0);
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
    //  AUDIT LOGS PANEL
    // ═══════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void showAuditLogs() {
        headerTitle.setText("Audit Logs");
        contentArea.getChildren().clear();

        VBox panel = new VBox(20);
        panel.setPadding(new Insets(0));

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.getStyleClass().add("action-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("System activity and change history");
        subtitle.getStyleClass().add("content-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("\uD83D\uDCE4 Export");
        exportBtn.getStyleClass().addAll("button", "btn-primary");
        exportBtn.setId("export-data");
        exportBtn.setOnAction(e -> showExportDialog());

        Button refreshBtn = new Button("\u21BB Refresh");
        refreshBtn.getStyleClass().add("button");
        refreshBtn.setOnAction(e -> showAuditLogs());

        toolbar.getChildren().addAll(subtitle, spacer, exportBtn, refreshBtn);

        // Table
        TableView<AuditLog> table = new TableView<>();
        table.setId("audit-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setMinHeight(500);

        TableColumn<AuditLog, Integer> colId = new TableColumn<>("Audit ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getAuditId()).asObject());
        colId.setMaxWidth(90);

        TableColumn<AuditLog, String> colAction = new TableColumn<>("Action");
        colAction.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getActionType()));
        colAction.setCellFactory(col -> new TableCell<>() {
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
                        case "INSERT": badge.getStyleClass().add("badge-approved"); break;
                        case "UPDATE": badge.getStyleClass().add("badge-pending"); break;
                        case "DELETE": badge.getStyleClass().add("badge-rejected"); break;
                        default:       badge.getStyleClass().add("badge-pending");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<AuditLog, String> colTable = new TableColumn<>("Affected Table");
        colTable.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAffectedTable()));

        TableColumn<AuditLog, Integer> colRecord = new TableColumn<>("Record ID");
        colRecord.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getRecordId()).asObject());

        TableColumn<AuditLog, String> colTimestamp = new TableColumn<>("Timestamp");
        colTimestamp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getActionTimestamp()));
        colTimestamp.setMinWidth(200);

        TableColumn<AuditLog, Integer> colUser = new TableColumn<>("User ID");
        colUser.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getUserId()).asObject());

        table.getColumns().addAll(colId, colAction, colTable, colRecord, colTimestamp, colUser);
        table.setItems(FXCollections.observableArrayList(auditDAO.getAllAuditLogs()));

        panel.getChildren().addAll(toolbar, table);
        contentArea.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_LEFT);
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

        Label titleLbl = new Label("Select Data to Export");
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e8e8f0;");

        Label descLbl = new Label("Choose a data source to export as a CSV file.");
        descLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #8888a8;");

        // Option cards in a VBox
        ToggleGroup group = new ToggleGroup();

        RadioButton optInventory  = buildExportOption(group, "📦", "Inventory",  "All equipment, status, specs, location and cost");
        RadioButton optRecords    = buildExportOption(group, "📄", "Records",    "All borrowing booking records with status");
        RadioButton optAuditLogs  = buildExportOption(group, "📋", "Audit Logs", "System activity: inserts, updates and deletes");

        optInventory.setSelected(true);

        VBox optionsBox = new VBox(10, optInventory, optRecords, optAuditLogs);

        content.getChildren().addAll(titleLbl, descLbl, optionsBox);
        dialogPane.setContent(content);

        // Style OK button
        dialogPane.lookupButton(ButtonType.OK).setStyle(
            "-fx-background-color: linear-gradient(to right,#4361ee,#3a86ff); -fx-text-fill: white; "
            + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 28;"
        );

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
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e8e8f0;");
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8888a8;");
        textBox.getChildren().addAll(titleLbl, descLbl);

        HBox card = new HBox(14, rb, textBox);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
            "-fx-background-color: #12121f; -fx-border-color: #2a2a45; -fx-border-radius: 10; "
            + "-fx-background-radius: 10; -fx-padding: 14 18;"
        );
        card.setOnMouseClicked(e -> rb.setSelected(true));
        card.setCursor(javafx.scene.Cursor.HAND);

        // Wrap the HBox into the RadioButton graphic so it renders as a card row
        // We return a proxy RadioButton that holds user data; the card is displayed separately.
        // Use a container trick: wrap card itself as graphic-less toggle handled by ToggleGroup.
        rb.setGraphic(textBox);
        rb.setStyle("-fx-text-fill: transparent; -fx-padding: 14 18; "
            + "-fx-background-color: #12121f; -fx-background-radius: 10; -fx-border-color: #2a2a45; "
            + "-fx-border-radius: 10; -fx-font-size: 0;");
        rb.setMaxWidth(Double.MAX_VALUE);
        rb.selectedProperty().addListener((obs, oldVal, selected) -> {
            if (selected) {
                rb.setStyle("-fx-text-fill: transparent; -fx-padding: 14 18; "
                    + "-fx-background-color: rgba(67,97,238,0.12); -fx-background-radius: 10; "
                    + "-fx-border-color: #4361ee; -fx-border-radius: 10; -fx-font-size: 0;");
            } else {
                rb.setStyle("-fx-text-fill: transparent; -fx-padding: 14 18; "
                    + "-fx-background-color: #12121f; -fx-background-radius: 10; "
                    + "-fx-border-color: #2a2a45; -fx-border-radius: 10; -fx-font-size: 0;");
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
                case "Audit Logs" -> {
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
}

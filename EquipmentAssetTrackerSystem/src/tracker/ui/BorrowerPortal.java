package tracker.ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import tracker.Main;
import tracker.dao.BookingDAO;
import tracker.dao.EquipmentDAO;
import tracker.models.Booking;
import tracker.models.Equipment;
import tracker.models.User;

import java.time.LocalDate;
import java.util.List;

public class BorrowerPortal {
    private Main mainApp;
    private User currentUser;
    private BorderPane view;
    private StackPane contentArea;
    private VBox sidebarBtnContainer;
    private Button activeSidebarBtn;
    private Label headerTitle;

    // DAOs
    private BookingDAO bookingDAO = new BookingDAO();
    private EquipmentDAO equipmentDAO = new EquipmentDAO();

    /**
     * Create the borrower portal.
     */
    public BorrowerPortal(Main mainApp, User user) {
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

        // Show bookings by default
        showMyBookings();
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

        Label brandSub = new Label("Borrower Portal");
        brandSub.getStyleClass().add("sidebar-brand-sub");

        brandBox.getChildren().addAll(brandName, brandSub);

        // Section label
        Label navLabel = new Label("NAVIGATION");
        navLabel.getStyleClass().add("sidebar-section-label");

        // Navigation buttons
        sidebarBtnContainer = new VBox(0);

        Button btnMyBookings = createSidebarButton("\uD83D\uDCC4  My Bookings", () -> showMyBookings());
        Button btnNewBooking = createSidebarButton("\u2795  New Booking", () -> showNewBooking());

        sidebarBtnContainer.getChildren().addAll(btnMyBookings, btnNewBooking);

        // Set first button active
        setActiveSidebarBtn(btnMyBookings);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer
        VBox footer = new VBox(8);
        footer.getStyleClass().add("sidebar-footer");

        Label footerUser = new Label("\uD83D\uDC64 " + currentUser.getUsername());
        footerUser.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 12px;");

        Label footerRole = new Label("BORROWER");
        footerRole.getStyleClass().add("header-role-badge");
        footerRole.setStyle("-fx-background-color: rgba(6, 214, 160, 0.15); -fx-text-fill: #06d6a0;");

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

        headerTitle = new Label("My Bookings");
        headerTitle.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("Logged in as  ");
        userLabel.getStyleClass().add("header-user");

        Label userName = new Label(currentUser.getUsername());
        userName.setStyle("-fx-text-fill: -text-primary; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label roleBadge = new Label("BORROWER");
        roleBadge.getStyleClass().add("header-role-badge");
        roleBadge.setStyle("-fx-background-color: rgba(6, 214, 160, 0.15); -fx-text-fill: #06d6a0;");

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("btn-logout");
        logoutBtn.setId("borrower-logout");
        logoutBtn.setOnAction(e -> mainApp.showLoginScreen());

        header.getChildren().addAll(headerTitle, spacer, userLabel, userName, roleBadge, logoutBtn);
        return header;
    }

    // ═══════════════════════════════════════════════════════════
    //  MY BOOKINGS PANEL
    // ═══════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void showMyBookings() {
        headerTitle.setText("My Bookings");
        contentArea.getChildren().clear();

        VBox panel = new VBox(20);
        panel.setPadding(new Insets(0));

        // Stats
        List<Booking> myBookings = bookingDAO.getBookingsByBorrower(currentUser.getUserId());
        long pending = myBookings.stream().filter(b -> "PENDING".equals(b.getBookingStatus())).count();
        long approved = myBookings.stream().filter(b -> "APPROVED".equals(b.getBookingStatus())).count();
        long returned = myBookings.stream().filter(b -> "RETURNED".equals(b.getBookingStatus())).count();

        HBox statsBar = new HBox(20);
        statsBar.setPadding(new Insets(0, 0, 10, 0));

        statsBar.getChildren().addAll(
            buildStatCard(String.valueOf(myBookings.size()), "Total Bookings", "#4361ee"),
            buildStatCard(String.valueOf(pending), "Pending", "#ffd166"),
            buildStatCard(String.valueOf(approved), "Approved", "#06d6a0"),
            buildStatCard(String.valueOf(returned), "Returned", "#118ab2")
        );

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.getStyleClass().add("action-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("View and track your equipment booking history");
        subtitle.getStyleClass().add("content-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("\u21BB Refresh");
        refreshBtn.getStyleClass().add("button");
        refreshBtn.setOnAction(e -> showMyBookings());

        toolbar.getChildren().addAll(subtitle, spacer, refreshBtn);

        // Table
        TableView<Booking> table = new TableView<>();
        table.setId("my-bookings-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setMinHeight(450);

        TableColumn<Booking, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getBookingId()).asObject());
        colId.setMaxWidth(70);

        TableColumn<Booking, Integer> colEquip = new TableColumn<>("Equipment ID");
        colEquip.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getEquipmentId()).asObject());

        TableColumn<Booking, String> colStart = new TableColumn<>("Start Date");
        colStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStartDatetime()));

        TableColumn<Booking, String> colReturn = new TableColumn<>("Expected Return");
        colReturn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getExpectedReturnDatetime()));

        TableColumn<Booking, String> colActualReturn = new TableColumn<>("Actual Return");
        colActualReturn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getActualReturnDatetime()));

        TableColumn<Booking, String> colPurpose = new TableColumn<>("Purpose");
        colPurpose.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPurposeDescription()));
        colPurpose.setMinWidth(200);

        TableColumn<Booking, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getBorrowingPrice()).asObject());
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("$%.2f", item));
            }
        });

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

        table.getColumns().addAll(colId, colEquip, colStart, colReturn, colActualReturn, colPurpose, colPrice, colStatus);
        table.setItems(FXCollections.observableArrayList(myBookings));

        panel.getChildren().addAll(statsBar, toolbar, table);
        contentArea.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_LEFT);
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

    // ═══════════════════════════════════════════════════════════
    //  NEW BOOKING PANEL
    // ═══════════════════════════════════════════════════════════

    private void showNewBooking() {
        headerTitle.setText("Request New Booking");
        contentArea.getChildren().clear();

        VBox panel = new VBox(24);
        panel.setPadding(new Insets(0));
        panel.setMaxWidth(700);

        // Form card
        VBox formCard = new VBox(20);
        formCard.getStyleClass().add("card");

        Label formTitle = new Label("New Booking Request");
        formTitle.getStyleClass().add("card-header");

        Label formSubtitle = new Label("Fill in the details below to request equipment borrowing");
        formSubtitle.getStyleClass().add("content-subtitle");

        // Equipment selector
        Label equipLabel = new Label("EQUIPMENT");
        equipLabel.getStyleClass().add("form-label");

        ComboBox<Equipment> equipmentBox = new ComboBox<>();
        List<Equipment> availableEquipment = equipmentDAO.getAllEquipment();
        availableEquipment.removeIf(eq -> !"AVAILABLE".equals(eq.getEquipmentStatus()));
        equipmentBox.setItems(FXCollections.observableArrayList(availableEquipment));
        equipmentBox.setPromptText("Select available equipment");
        equipmentBox.setMaxWidth(Double.MAX_VALUE);

        // Start date
        Label startLabel = new Label("START DATE");
        startLabel.getStyleClass().add("form-label");

        DatePicker startDate = new DatePicker();
        startDate.setPromptText("Select start date");
        startDate.setMaxWidth(Double.MAX_VALUE);
        startDate.setValue(LocalDate.now());

        // Return date
        Label returnLabel = new Label("EXPECTED RETURN DATE");
        returnLabel.getStyleClass().add("form-label");

        DatePicker returnDate = new DatePicker();
        returnDate.setPromptText("Select return date");
        returnDate.setMaxWidth(Double.MAX_VALUE);
        returnDate.setValue(LocalDate.now().plusDays(7));

        // Purpose
        Label purposeLabel = new Label("PURPOSE / REASON");
        purposeLabel.getStyleClass().add("form-label");

        TextArea purposeArea = new TextArea();
        purposeArea.setPromptText("Describe why you need this equipment...");
        purposeArea.setPrefRowCount(4);
        purposeArea.setMaxWidth(Double.MAX_VALUE);

        // Feedback label
        Label feedbackLabel = new Label();
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);

        // Submit button
        Button submitBtn = new Button("Submit Booking Request");
        submitBtn.getStyleClass().addAll("btn-primary");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setId("submit-booking");

        submitBtn.setOnAction(e -> {
            if (equipmentBox.getValue() == null) {
                showFeedback(feedbackLabel, "Please select equipment.", "-danger");
                return;
            }
            if (startDate.getValue() == null || returnDate.getValue() == null) {
                showFeedback(feedbackLabel, "Please select both dates.", "-danger");
                return;
            }
            if (purposeArea.getText().trim().isEmpty()) {
                showFeedback(feedbackLabel, "Please enter a purpose.", "-danger");
                return;
            }

            Booking booking = new Booking();
            booking.setEquipmentId(equipmentBox.getValue().getEquipmentId());
            booking.setBorrowerId(currentUser.getUserId());
            booking.setStartDatetime(startDate.getValue().toString());
            booking.setExpectedReturnDatetime(returnDate.getValue().toString());
            booking.setPurposeDescription(purposeArea.getText().trim());
            booking.setBookingStatus("PENDING");

            if (bookingDAO.addBooking(booking)) {
                showFeedback(feedbackLabel, "\u2714 Booking submitted successfully! Awaiting admin approval.", "-success");
                equipmentBox.setValue(null);
                purposeArea.clear();
                startDate.setValue(LocalDate.now());
                returnDate.setValue(LocalDate.now().plusDays(7));
            } else {
                showFeedback(feedbackLabel, "Failed to submit booking. Please try again.", "-danger");
            }
        });

        formCard.getChildren().addAll(
            formTitle, formSubtitle,
            new Separator(),
            equipLabel, equipmentBox,
            startLabel, startDate,
            returnLabel, returnDate,
            purposeLabel, purposeArea,
            feedbackLabel,
            submitBtn
        );

        panel.getChildren().add(formCard);
        contentArea.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_LEFT);
    }

    private void showFeedback(Label label, String message, String colorVar) {
        label.setText(message);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + colorVar + ";");
        label.setVisible(true);
        label.setManaged(true);
    }
}

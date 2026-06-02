package tracker.ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
    private HBox navBtnContainer;
    private Button activeNavBtn;
    private Label headerTitle;
    private Button btnMyBookings;
    private Button btnNewBooking;

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

        // ── Header Navbar ──
        HBox header = buildHeader();
        view.setTop(header);

        // ── Main Content Container ──
        VBox mainContentLayout = new VBox(20);
        mainContentLayout.setPadding(new Insets(24, 32, 24, 32));
        mainContentLayout.setStyle("-fx-background-color: -bg-primary;");

        headerTitle = new Label("My Bookings");
        headerTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: -text-primary;");

        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        contentArea.setPadding(new Insets(0));
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        mainContentLayout.getChildren().addAll(headerTitle, contentArea);

        ScrollPane scrollPane = new ScrollPane(mainContentLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        view.setCenter(scrollPane);

        // Show bookings by default
        showMyBookings();
    }

    // ═══════════════════════════════════════════════════════════
    //  HEADER / NAVIGATION
    // ═══════════════════════════════════════════════════════════

    private HBox buildHeader() {
        HBox header = new HBox(24);
        header.getStyleClass().add("header-bar");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 32, 12, 32));

        // Horizontal Nav Container
        navBtnContainer = new HBox(10);
        navBtnContainer.setAlignment(Pos.CENTER_LEFT);

        btnMyBookings = createNavButton("📄 My Bookings", () -> showMyBookings());
        btnNewBooking = createNavButton("➕ New Booking", () -> showNewBooking());

        navBtnContainer.getChildren().addAll(btnMyBookings, btnNewBooking);

        // Initialize active state
        setActiveNavBtn(btnMyBookings);

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

        header.getChildren().addAll(navBtnContainer, spacer, userLabel, userName, roleBadge, logoutBtn);
        return header;
    }

    private Button createNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("header-nav-btn");
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

        Button addBtn = new Button("+ Add Booking");
        addBtn.getStyleClass().addAll("button", "btn-success");
        addBtn.setId("add-booking-borrower");
        addBtn.setOnAction(e -> {
            if (btnNewBooking != null) {
                setActiveNavBtn(btnNewBooking);
            }
            showNewBooking();
        });

        Button refreshBtn = new Button("\u21BB Refresh");
        refreshBtn.getStyleClass().add("button");
        refreshBtn.setOnAction(e -> showMyBookings());

        toolbar.getChildren().addAll(subtitle, spacer, addBtn, refreshBtn);

        // Table
        TableView<Booking> table = new TableView<>();
        table.setId("my-bookings-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setMinHeight(450);

        // Load equipment lookup map dynamically
        java.util.Map<Integer, String> equipmentMap = equipmentDAO.getAllEquipment().stream()
            .collect(java.util.stream.Collectors.toMap(Equipment::getEquipmentId, Equipment::getSerialNumber, (a, b) -> a));

        TableColumn<Booking, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getBookingId()).asObject());
        colId.setMaxWidth(70);

        TableColumn<Booking, String> colEquipName = new TableColumn<>("Equipment Name");
        colEquipName.setCellValueFactory(c -> {
            int equipId = c.getValue().getEquipmentId();
            String name = equipmentMap.getOrDefault(equipId, "Unknown (" + equipId + ")");
            return new SimpleStringProperty(name);
        });

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
                setText(empty || item == null ? "" : String.format("\u20B1%.2f", item));
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

        table.getColumns().addAll(colId, colEquipName, colStart, colReturn, colActualReturn, colPurpose, colPrice, colStatus);
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
        equipmentBox.setEditable(true);
        equipmentBox.setPromptText("Type to search available equipment...");
        equipmentBox.setMaxWidth(Double.MAX_VALUE);

        List<Equipment> availableEquipment = equipmentDAO.getAllEquipment();
        availableEquipment.removeIf(eq -> !"AVAILABLE".equals(eq.getEquipmentStatus()));
        javafx.collections.ObservableList<Equipment> originalList = FXCollections.observableArrayList(availableEquipment);
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
                return availableEquipment.stream()
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
                javafx.collections.ObservableList<Equipment> filteredList = FXCollections.observableArrayList();
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

        // Start date
        Label startLabel = new Label("START DATE & TIME");
        startLabel.getStyleClass().add("form-label");

        DatePicker startDate = new DatePicker();
        startDate.setPromptText("Select start date");
        startDate.setMaxWidth(Double.MAX_VALUE);
        startDate.setValue(LocalDate.now());
        
        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, java.time.LocalTime.now().getHour());
        startHourSpinner.setEditable(true);
        startHourSpinner.setPrefWidth(75);
        startHourSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL);

        Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59, java.time.LocalTime.now().getMinute());
        startMinuteSpinner.setEditable(true);
        startMinuteSpinner.setPrefWidth(75);
        startMinuteSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL);

        Label startColonLabel = new Label(":");
        startColonLabel.setStyle("-fx-text-fill: -text-primary; -fx-font-size: 16px; -fx-font-weight: bold;");

        HBox startTimeBox = new HBox(6, startDate, startHourSpinner, startColonLabel, startMinuteSpinner);
        startTimeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

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
            Equipment selectedEquip = equipmentBox.getValue();
            if (selectedEquip == null) {
                String enteredText = equipmentBox.getEditor().getText().trim();
                selectedEquip = availableEquipment.stream()
                    .filter(eq -> {
                        String fullDisplay = eq.getEquipmentName() + " (" + eq.getTechnicalSpecifications() + ")";
                        return fullDisplay.equalsIgnoreCase(enteredText) || 
                               (eq.getEquipmentName() != null && eq.getEquipmentName().equalsIgnoreCase(enteredText));
                    })
                    .findFirst()
                    .orElse(null);
            }

            if (selectedEquip == null) {
                showFeedback(feedbackLabel, "Please select or type a valid available equipment.", "-danger");
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
            booking.setEquipmentId(selectedEquip.getEquipmentId());
            booking.setBorrowerId(currentUser.getUserId());
            
            String startTimeStr = String.format("%02d:%02d", startHourSpinner.getValue(), startMinuteSpinner.getValue());
            booking.setStartDatetime(startDate.getValue().toString() + " " + startTimeStr);
            
            booking.setExpectedReturnDatetime(returnDate.getValue().toString());
            booking.setPurposeDescription(purposeArea.getText().trim());
            booking.setBookingStatus("PENDING");

            if (bookingDAO.addBooking(booking)) {
                showFeedback(feedbackLabel, "\u2714 Booking submitted successfully! Awaiting admin approval.", "-success");
                equipmentBox.setValue(null);
                equipmentBox.getEditor().clear();
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
            startLabel, startTimeBox,
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

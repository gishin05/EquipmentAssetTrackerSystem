package tracker;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import tracker.db.DatabaseManager;
import tracker.models.User;
import tracker.ui.LoginScreen;
import tracker.ui.AdminDashboard;
import tracker.ui.BorrowerPortal;

public class Main extends Application {

    private Stage primaryStage;
    private StackPane rootContainer;
    private Scene scene;

    /**
     * Launch the application.W
     */
    public static void main(String[] args) {
        // Initialize the embedded SQLite database and tables
        DatabaseManager.initializeDatabase();
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        rootContainer = new StackPane();
        // Ensure the root container uses the desired app canvas color and CSS root class
        rootContainer.getStyleClass().add("root");
        rootContainer.setStyle("-fx-background-color: #f4f2fc;");
        // Add a large, non-interactive background card so the app canvas color
        // can be changed without covering interactive content. It sits behind
        // everything else and is mouseTransparent so it won't intercept events.
        javafx.scene.layout.Region bgCard = new javafx.scene.layout.Region();
        bgCard.getStyleClass().add("app-bg-card");
        bgCard.setMouseTransparent(true);
        // Bind the background card size to the root with some margin so it never
        // fully covers floating UI elements (keeps it visually behind content).
        bgCard.prefWidthProperty().bind(rootContainer.widthProperty().subtract(120));
        bgCard.prefHeightProperty().bind(rootContainer.heightProperty().subtract(120));
        // Put the bgCard first so other children are drawn on top
        rootContainer.getChildren().add(bgCard);
        scene = new Scene(rootContainer, 1920, 1080);
        // Ensure the Scene background matches the theme variable (lavender)
        scene.setFill(Color.web("#f4f2fc"));

        // Load the CSS stylesheet
        String css = getClass().getResource("/tracker/ui/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setTitle("Equipment Asset Tracker System");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(720);

        showLoginScreen();

        primaryStage.show();
    }

    public void showLoginScreen() {
        rootContainer.getChildren().clear();
        LoginScreen loginScreen = new LoginScreen(this);
        rootContainer.getChildren().add(loginScreen.getView());
    }

    public void showAdminDashboard(User user) {
        rootContainer.getChildren().clear();
        AdminDashboard adminDashboard = new AdminDashboard(this, user);
        rootContainer.getChildren().add(adminDashboard.getView());
    }

    public void showBorrowerPortal(User user) {
        rootContainer.getChildren().clear();
        BorrowerPortal borrowerPortal = new BorrowerPortal(this, user);
        rootContainer.getChildren().add(borrowerPortal.getView());
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}

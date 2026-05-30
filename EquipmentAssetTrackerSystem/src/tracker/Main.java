package tracker;

import javafx.application.Application;
import javafx.scene.Scene;
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
     * Launch the application.
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
        scene = new Scene(rootContainer, 1920, 1080);

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

package visualization;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class GraphApp extends Application {

    private GraphManagerFX graphManager;

    @Override
    public void start(Stage primaryStage) {
        Pane mainPane = new Pane();
        graphManager = new GraphManagerFX(mainPane);

        setupUserInteractions(mainPane);

        Scene scene = new Scene(mainPane, 600, 400);
        primaryStage.setTitle("Interactive Graph");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupUserInteractions(Pane pane) {
//        pane.setOnMouseClicked(event -> {
//            if (event.getButton() == MouseButton.PRIMARY) {
//                graphManager.handlePrimaryClick(event.getX(), event.getY());
//            } else if (event.getButton() == MouseButton.SECONDARY) {
//                graphManager.handleSecondaryClick(event.getX(), event.getY());
//            }
//        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}

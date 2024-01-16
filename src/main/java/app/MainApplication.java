package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        double sceneWidth = 800; // Scene width
        double sceneHeight = 600; // Scene height

        Scene scene = new Scene(new Pane(), sceneWidth, sceneHeight);
        AppController controller = new AppController(scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Game Theory Flow Simulation");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


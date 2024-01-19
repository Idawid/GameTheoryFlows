package app;

import app.GameObjects.ConnectionMatrix;
import app.GameObjects.Path;
import app.managers.graph.common.Vertex;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import org.javatuples.Pair;

import java.util.List;

public class MainApplication extends Application {
    private static final double INITIAL_WIDTH = 800;
    private static final double INITIAL_HEIGHT = 600;
    private static final double ASPECT_RATIO = INITIAL_WIDTH / INITIAL_HEIGHT;

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(new Pane(), INITIAL_WIDTH, INITIAL_HEIGHT);
        AppController controller = new AppController(scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Game Theory Flow Simulation");
        primaryStage.show();

        primaryStage.setMinWidth(INITIAL_WIDTH);
        primaryStage.setMinHeight(INITIAL_HEIGHT);

        // Disable maximization
        primaryStage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
            primaryStage.setMaximized(false);
        });

        // Scale transformation
        Scale scale = new Scale(1, 1, 0, 0);
        scene.getRoot().getTransforms().add(scale);

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            double newWidth = newVal.doubleValue();
            double scaleFactor = newWidth / INITIAL_WIDTH;
            scale.setX(scaleFactor);
            scale.setY(scaleFactor);
            primaryStage.setHeight(newWidth / ASPECT_RATIO); // Constant aspect ratio
        });

        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            double newHeight = newVal.doubleValue();
            double scaleFactor = newHeight / INITIAL_HEIGHT;
            scale.setX(scaleFactor);
            scale.setY(scaleFactor);
            primaryStage.setWidth(newHeight * ASPECT_RATIO); // Constant aspect ratio
        });


        ConnectionMatrix test1 = new ConnectionMatrix(6);
        test1.addParameters(1.0, 0.0, 10.0, 0, 1);
        test1.addParameters(2.0, 2.0, 12.0, 0, 2);
        test1.addParameters(1.2, 1.0, 0.0, 0, 3);
        test1.addParameters(1.2, 1.0, 1.1, 1, 2);
        test1.addParameters(1.1, 2.0, 1.2, 1, 4);
        test1.addParameters(0.0, 2.0, 0.0, 2, 4);
        test1.addParameters(0.0, 4.0, 0.0, 4, 5);
        test1.addParameters(2.0, 0.0, 0.0, 3, 5);
        test1.getAllPaths();
        for(Path p : test1.getPaths()){
            System.out.println(p.getRoute());
        }
        System.out.println("------------");
        test1.randomPayloadDiscrete(100);
        while(test1.simulationStepDiscrete()) {

        }
        controller.drawGraph(test1.getPaths(),test1.getNumberOfVertices());
    }

    public static void main(String[] args) {
        launch(args);
    }
}


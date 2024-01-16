//package visualization;
//
//import javafx.scene.control.Label;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Circle;
//
//public class VertexFX extends Circle {
//    private static final double RADIUS = 10;
//    private Label label;
//
//    public VertexFX(double x, double y, String labelText) {
//        super(x, y, RADIUS, Color.WHITE);
//        setStroke(Color.BLACK);
//        createLabel(labelText);
//    }
//
//    private void createLabel(String text) {
//        label = new Label(text);
//        label.layoutXProperty().bind(centerXProperty().subtract(label.widthProperty().divide(2)));
//        label.layoutYProperty().bind(centerYProperty().subtract(label.heightProperty().divide(2)));
//        label.setMouseTransparent(true);
//    }
//
//    public Label getLabel() {
//        return label;
//    }
//
//    // Additional methods specific to this JavaFX vertex component
//}

//package visualization;
//
//import javafx.scene.paint.Color;
//import javafx.scene.shape.QuadCurve;
//
//public class EdgeFX extends QuadCurve {
//    public EdgeFX(VertexFX start, VertexFX end) {
//        setStroke(Color.BLACK);
//        setStrokeWidth(2);
//        setFill(null);
//
//        bindToVertices(start, end);
//    }
//
//    private void bindToVertices(VertexFX start, VertexFX end) {
//        startXProperty().bind(start.centerXProperty());
//        startYProperty().bind(start.centerYProperty());
//        endXProperty().bind(end.centerXProperty());
//        endYProperty().bind(end.centerYProperty());
//        controlXProperty().bind(start.centerXProperty().add(end.centerXProperty()).divide(2));
//        controlYProperty().bind(start.centerYProperty().add(end.centerYProperty()).divide(2));
//    }
//
//    // Additional methods specific to this JavaFX edge component
//}

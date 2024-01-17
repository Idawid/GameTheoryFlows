package app.managers.view;

import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
import app.managers.view.common.SelectionResult;
import app.managers.view.common.SelectionType;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

public class GraphViewManager {
    private Pane graphView;
    private Map<Vertex, Group> vertexGraphicsMap;
    private Map<Edge, Line> edgeGraphicsMap;
    private final double gridSpacing = 20;

    private Line tempEdgeLine;

    public GraphViewManager(Scene scene) {
        this.vertexGraphicsMap = new HashMap<>();
        this.edgeGraphicsMap = new HashMap<>();
        this.graphView = new Pane();
        scene.setRoot(graphView);

        createGridBackground(scene);
    }

    private void createGridBackground(Scene scene) {
        Canvas gridCanvas = new Canvas();
        gridCanvas.widthProperty().bind(scene.widthProperty());
        gridCanvas.heightProperty().bind(scene.heightProperty());
        drawGrid(gridCanvas);
        graphView.getChildren().add(0, gridCanvas);
    }

    private void drawGrid(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        redrawGrid(canvas);

        canvas.widthProperty().addListener(evt -> redrawGrid(canvas));
        canvas.heightProperty().addListener(evt -> redrawGrid(canvas));
    }

    private void redrawGrid(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double width = canvas.getWidth();
        double height = canvas.getHeight();

        for (double i = gridSpacing; i < width; i += gridSpacing) {
            gc.strokeLine(i, 0, i, height);
        }
        for (double i = gridSpacing; i < height; i += gridSpacing) {
            gc.strokeLine(0, i, width, i);
        }
    }

    public void drawVertex(Vertex vertex) {
        Group vertexGroup = vertexGraphicsMap.get(vertex);
        if (vertexGroup == null) {
            Circle vertexCircle = new Circle(vertex.getX(), vertex.getY(), 15);
            vertexCircle.setFill(Color.WHITE);
            vertexCircle.setStroke(Color.BLACK);
            vertexCircle.setStrokeWidth(2);

            Text vertexIdText = new Text(String.valueOf(vertex.getId()));
            vertexIdText.setX(vertex.getX() - vertexIdText.getBoundsInLocal().getWidth() / 2);
            vertexIdText.setY(vertex.getY() + vertexIdText.getBoundsInLocal().getHeight() / 4);

            vertexGroup = new Group(vertexCircle, vertexIdText);
            vertexGraphicsMap.put(vertex, vertexGroup);
            graphView.getChildren().add(vertexGroup);
        } else {
            updateVertexPosition(vertex, vertexGroup);
        }
    }

    private void updateVertexPosition(Vertex vertex, Group vertexGroup) {
        Circle vertexCircle = (Circle) vertexGroup.getChildren().get(0);
        Text vertexIdText = (Text) vertexGroup.getChildren().get(1);
        vertexCircle.setCenterX(vertex.getX());
        vertexCircle.setCenterY(vertex.getY());
        vertexIdText.setX(vertex.getX() - vertexIdText.getBoundsInLocal().getWidth() / 2);
        vertexIdText.setY(vertex.getY() + vertexIdText.getBoundsInLocal().getHeight() / 4);
    }

    public void drawEdge(Edge edge) {
        Line edgeLine = edgeGraphicsMap.get(edge);
        if (edgeLine == null) {
            edgeLine = new Line(edge.getFrom().getX(), edge.getFrom().getY(), edge.getTo().getX(), edge.getTo().getY());
            edgeLine.setStroke(Color.BLACK);
            edgeLine.setStrokeWidth(1.0);

            edgeGraphicsMap.put(edge, edgeLine);
            graphView.getChildren().add(edgeLine);
        } else {
            updateEdgePosition(edge, edgeLine);
        }
    }

    private void updateEdgePosition(Edge edge, Line edgeLine) {
        edgeLine.setStartX(edge.getFrom().getX());
        edgeLine.setStartY(edge.getFrom().getY());
        edgeLine.setEndX(edge.getTo().getX());
        edgeLine.setEndY(edge.getTo().getY());
    }

    public void highlightVertex(Vertex vertex) {
        resetVertexSelection();

        // Highlight the selected vertex
        Group selectedGroup = vertexGraphicsMap.get(vertex);
        if (selectedGroup != null) {
            Circle vertexCircle = (Circle) selectedGroup.getChildren().get(0);
            vertexCircle.setStroke(Color.RED); // Highlight with red border
            vertexCircle.setStrokeWidth(3.0); // Increase border width for highlight
        }
    }

    public void highlightEdge(Edge edge) {
        resetEdgeHighlight();

        // Highlight the selected edge
        Line edgeLine = edgeGraphicsMap.get(edge);
        if (edgeLine != null) {
            edgeLine.setStroke(Color.BLUE); // Highlight with blue color
            edgeLine.setStrokeWidth(2.0); // Increase line width for highlight
        }
    }

    public void resetVertexSelection() {
        for (Group group : vertexGraphicsMap.values()) {
            Circle circle = (Circle) group.getChildren().get(0);
            circle.setStroke(Color.BLACK); // Reset to default appearance
            circle.setStrokeWidth(2.0); // Reset to default stroke width
        }
    }

    public void resetEdgeHighlight() {
        for (Line line : edgeGraphicsMap.values()) {
            line.setStroke(Color.BLACK); // Reset to default appearance
            line.setStrokeWidth(1.0); // Reset to default stroke width
        }
    }

    public SelectionResult selectElementAt(double x, double y, double selectionThreshold) {
        // Check if a vertex is clicked
        for (Map.Entry<Vertex, Group> entry : getVertexGraphicsMap().entrySet()) {
            Vertex vertex = entry.getKey();
            Circle circle = (Circle) entry.getValue().getChildren().get(0);

            if (isNearVertex(circle, x, y, selectionThreshold)) {
                return new SelectionResult(SelectionType.VERTEX, vertex);
            }
        }

        // Check if an edge is clicked
        for (Map.Entry<Edge, Line> entry : getEdgeGraphicsMap().entrySet()) {
            Edge edge = entry.getKey();
            Line line = entry.getValue();

            if (isNearEdge(line, x, y, selectionThreshold)) {
                return new SelectionResult(SelectionType.EDGE, edge);
            }
        }

        return new SelectionResult(SelectionType.NONE, null);
    }

    private boolean isNearEdge(Line line, double x, double y, double threshold) {
        double x1 = line.getStartX();
        double y1 = line.getStartY();
        double x2 = line.getEndX();
        double y2 = line.getEndY();

        double dx = x2 - x1;
        double dy = y2 - y1;
        double lineLengthSquared = dx * dx + dy * dy;

        double t = ((x - x1) * dx + (y - y1) * dy) / lineLengthSquared;
        t = Math.max(0, Math.min(1, t)); // Clamp t to the range [0, 1]

        double closestX = x1 + t * dx;
        double closestY = y1 + t * dy;

        dx = x - closestX;
        dy = y - closestY;

        return (dx * dx + dy * dy) < threshold * threshold;
    }

    public boolean isNearVertex(Circle circle, double x, double y, double threshold) {
        double distance = Math.hypot(circle.getCenterX() - x, circle.getCenterY() - y);
        return distance <= threshold;
    }

    public void startDrawingEdge(double startX, double startY) {
        tempEdgeLine = new Line(startX, startY, startX, startY);
        graphView.getChildren().add(tempEdgeLine);

    }

    public void updateDrawingEdge(double endX, double endY) {
        if (tempEdgeLine != null) {
            tempEdgeLine.setEndX(endX);
            tempEdgeLine.setEndY(endY);
        }
    }

    public void stopDrawingEdge() {
        if (tempEdgeLine != null) {
            graphView.getChildren().remove(tempEdgeLine);
            tempEdgeLine = null;
        }
    }

    // Getters and setters for vertexGraphicsMap and edgeGraphicsMap

    public Pane getGraphView() {
        return graphView;
    }

    public Map<Vertex, Group> getVertexGraphicsMap() {
        return vertexGraphicsMap;
    }

    public Map<Edge, Line> getEdgeGraphicsMap() {
        return edgeGraphicsMap;
    }
}


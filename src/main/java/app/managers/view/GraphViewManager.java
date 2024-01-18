package app.managers.view;

import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
import app.managers.view.common.SelectionResult;
import app.managers.view.common.SelectionType;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphViewManager {
    private Pane graphView;
    private Pane edgeLayer;
    private Pane vertexLayer;
    private Map<Vertex, Group> vertexGraphicsMap;
    private Map<Edge, Group> edgeGraphicsMap;
    private final double gridSpacing = 20;

    private Line tempEdgeLine;

    public GraphViewManager(Scene scene) {
        this.vertexGraphicsMap = new HashMap<>();
        this.edgeGraphicsMap = new HashMap<>();

        this.graphView = new Pane();
        this.edgeLayer = new Pane();
        this.vertexLayer = new Pane();

        graphView.getChildren().addAll(edgeLayer, vertexLayer);
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
            // Circle
            Circle vertexCircle = new Circle(vertex.getX(), vertex.getY(), 15);
            vertexCircle.setFill(Color.WHITE);
            vertexCircle.setStroke(Color.BLACK);
            vertexCircle.setStrokeWidth(2);

            // Text inside
            Text vertexIdText = new Text(String.valueOf(vertex.getId()));
            vertexIdText.setFont(Font.font("Gill Sans MT Bold", FontWeight.BOLD, 20));
            vertexIdText.setOpacity(0.85);

            double textWidth = vertexIdText.getLayoutBounds().getWidth(); // Get the width of the text
            double textHeight = vertexIdText.getLayoutBounds().getHeight(); // Get the height of the text

            vertexIdText.setX(vertex.getX() - textWidth / 2);
            vertexIdText.setY(vertex.getY() + textHeight / 4);

            //
            vertexGroup = new Group(vertexCircle, vertexIdText);
            vertexGraphicsMap.put(vertex, vertexGroup);
            vertexLayer.getChildren().add(vertexGroup);
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
        Group edgeGroup = edgeGraphicsMap.get(edge);
        if (edgeGroup == null) {
            edgeGroup = new Group();

            // Create the border lines
            Line line1 = new Line(edge.getFrom().getX(), edge.getFrom().getY(), edge.getTo().getX(), edge.getTo().getY());
            Line line2 = new Line(edge.getFrom().getX(), edge.getFrom().getY(), edge.getTo().getX(), edge.getTo().getY());
            Line middleLine = new Line(edge.getFrom().getX(), edge.getFrom().getY(), edge.getTo().getX(), edge.getTo().getY());

            // Set properties for the border lines
            double offset = 2.0; // Offset for the border lines
            adjustLinePosition(line1, edge, offset);
            adjustLinePosition(line2, edge, -offset);
            line1.setStrokeWidth(1);
            line2.setStrokeWidth(1);

            // Set properties for the middle line
            middleLine.setStroke(Color.YELLOW);
            middleLine.setStrokeWidth(3.0); // Thicker line

            edgeGroup.setOpacity(0.9);
            edgeGroup.getChildren().addAll(line1, middleLine, line2);
            edgeGraphicsMap.put(edge, edgeGroup);
            edgeLayer.getChildren().add(edgeGroup);
        } else {
            // Update existing edge position
            updateEdgePosition(edge, edgeGroup);
        }
    }

    private void adjustLinePosition(Line line, Edge edge, double offset) {
        double angle = Math.atan2(edge.getTo().getY() - edge.getFrom().getY(), edge.getTo().getX() - edge.getFrom().getX());
        line.setStartX(line.getStartX() + offset * Math.cos(angle + Math.PI / 2));
        line.setStartY(line.getStartY() + offset * Math.sin(angle + Math.PI / 2));
        line.setEndX(line.getEndX() + offset * Math.cos(angle + Math.PI / 2));
        line.setEndY(line.getEndY() + offset * Math.sin(angle + Math.PI / 2));
    }

    private void updateEdgePosition(Edge edge, Group edgeGroup) {
//        edgeGroup.setStartX(edge.getFrom().getX());
//        edgeGroup.setStartY(edge.getFrom().getY());
//        edgeGroup.setEndX(edge.getTo().getX());
//        edgeGroup.setEndY(edge.getTo().getY());
    }

    public void highlightVertex(Vertex vertex) {
        resetVertexSelection();

        // Highlight the selected vertex
        Group vertexGroup = vertexGraphicsMap.get(vertex);
        if (vertexGroup != null) {
            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.DARKGRAY);
            dropShadow.setRadius(2);
            dropShadow.setSpread(10);

            vertexGroup.setEffect(dropShadow);
        }
    }

    public void highlightEdge(Edge edge) {
        resetEdgeHighlight();

        Group edgeGroup = edgeGraphicsMap.get(edge);
        if (edgeGroup != null) {
            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.DARKGRAY);
            dropShadow.setRadius(2);
            dropShadow.setSpread(10);

            edgeGroup.setEffect(dropShadow); // Apply drop shadow effect for highlighting
        }
    }

    public void resetEdgeHighlight() {
        for (Group edgeGroup : edgeGraphicsMap.values()) {
            edgeGroup.setEffect(null); // Remove any effects to reset the highlight
        }
    }

    public void resetVertexSelection() {
        for (Group group : vertexGraphicsMap.values()) {
            group.setEffect(null);
        }
    }

    public void undrawVertex(Vertex vertex) {
        Group vertexGroup = vertexGraphicsMap.get(vertex);
        if (vertexGroup != null) {
            vertexLayer.getChildren().remove(vertexGroup); // Remove the vertex from the vertex layer
            vertexGraphicsMap.remove(vertex); // Remove the vertex from the map

            // Remove all edges connected to this vertex
            List<Edge> connectedEdges = getConnectedEdges(vertex);
            connectedEdges.forEach(this::undrawEdge);
        }
    }

    public void undrawEdge(Edge edge) {
        Group edgeGroup = edgeGraphicsMap.get(edge);
        if (edgeGroup != null) {
            edgeLayer.getChildren().remove(edgeGroup); // Remove the edge from the edge layer
            edgeGraphicsMap.remove(edge); // Remove the edge from the map
        }
    }

    public List<Edge> getConnectedEdges(Vertex vertex) {
        List<Edge> connectedEdges = new ArrayList<>();
        for (Edge edge : edgeGraphicsMap.keySet()) {
            if (edge.getFrom().equals(vertex) || edge.getTo().equals(vertex)) {
                connectedEdges.add(edge);
            }
        }
        return connectedEdges;
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
        for (Map.Entry<Edge, Group> entry : getEdgeGraphicsMap().entrySet()) {
            Edge edge = entry.getKey();
            Group edgeGroup = entry.getValue();

            for (Node node : edgeGroup.getChildren()) {
                if (node instanceof Line) {
                    Line line = (Line) node;
                    if (isNearEdge(line, x, y, selectionThreshold)) {
                        return new SelectionResult(SelectionType.EDGE, edge);
                    }
                }
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
        edgeLayer.getChildren().add(tempEdgeLine);

    }

    public void updateDrawingEdge(double endX, double endY) {
        if (tempEdgeLine != null) {
            tempEdgeLine.setEndX(endX);
            tempEdgeLine.setEndY(endY);
        }
    }

    public void stopDrawingEdge() {
        if (tempEdgeLine != null) {
            edgeLayer.getChildren().remove(tempEdgeLine);
            tempEdgeLine = null;
        }
    }

    // Getters and setters for vertexGraphicsMap and edgeGraphicsMap

    public Pane getGraphView() {
        return graphView;
    }

    public Pane getVertexLayer() {
        return vertexLayer;
    }
    public Pane getEdgeLayer() {
        return edgeLayer;
    }
    public Map<Vertex, Group> getVertexGraphicsMap() {
        return vertexGraphicsMap;
    }

    public Map<Edge, Group> getEdgeGraphicsMap() {
        return edgeGraphicsMap;
    }
}


package app.managers.view;

import app.Observer;
import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
import app.managers.graph.flow.FlowEdge;
import app.managers.graph.flow.FlowVertex;
import app.managers.view.common.SelectionResult;
import app.managers.view.common.SelectionType;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GraphViewManager implements Observer {
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

        // Allow mouse events to pass through the panes' transparent areas
        graphView.setPickOnBounds(false);
        edgeLayer.setPickOnBounds(false);
        vertexLayer.setPickOnBounds(false);

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
            vertexGroup = new Group();

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

            // Add event handlers
            if (vertex instanceof FlowVertex) {
                FlowVertex flowVertex = (FlowVertex) vertex;
                vertexIdText.setText(flowVertex.toString());

                if (flowVertex.isSource()) {
                    vertexCircle.setFill(Color.LIGHTGRAY);
                }

                vertexGroup.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                        // Handle double-click event
                        handleVertexDoubleClick(flowVertex);
                    }
                    if (event.getButton() == MouseButton.SECONDARY) {
                        handleVertexDoubleClick(flowVertex);
                        event.consume();
                    }
                });
            }

            //
            vertexGroup.getChildren().addAll(vertexCircle, vertexIdText);
            vertexGraphicsMap.put(vertex, vertexGroup);
            vertexLayer.getChildren().add(vertexGroup);
        } else {
            updateVertexPosition(vertex, vertexGroup);
        }
    }

    private void handleVertexDoubleClick(FlowVertex flowVertex) {
        // Dialog or series of dialogs to set isSource, isSink, and flowCapacity
        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("None", "Source", "Sink", "None");
        choiceDialog.setTitle("Vertex Configuration");
        choiceDialog.setHeaderText("Set Vertex Type");
        choiceDialog.setContentText("Choose the vertex type:");

        Optional<String> result = choiceDialog.showAndWait();
        result.ifPresent(choice -> {
            switch (choice) {
                case "Source":
                    flowVertex.setSource(true);
                    flowVertex.setSink(false);
                    askForFlowCapacity(flowVertex);
                    break;
                case "Sink":
                    flowVertex.setSink(true);
                    flowVertex.setSource(false);
                    break;
                case "None":
                    flowVertex.setSource(false);
                    flowVertex.setSink(false);
                    break;
            }
        });
    }

    private void askForFlowCapacity(FlowVertex flowVertex) {
        TextInputDialog capacityDialog = new TextInputDialog("0");
        capacityDialog.setTitle("Flow Capacity");
        capacityDialog.setHeaderText("Set Flow Capacity");
        capacityDialog.setContentText("Enter positive flow capacity:");

        Optional<String> capacityResult = capacityDialog.showAndWait();
        capacityResult.ifPresent(capacity -> {
            try {
                double flowCapacity = Double.parseDouble(capacity);
                if (flowCapacity > 0) {
                    flowVertex.setFlowCapacity(flowCapacity);
                } else {
                    // Handle invalid input (e.g., show error message)
                }
            } catch (NumberFormatException e) {
                // Handle invalid input (e.g., show error message)
            }
        });
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

            // 1. Create Line Elements
            Group lineGroup = createLineElements(edge);
            edgeGroup.getChildren().add(lineGroup);

            // 2. Create and Position Text (if FlowEdge)
            if (edge instanceof FlowEdge) {
                FlowEdge flowEdge = (FlowEdge) edge;
                Node costFunctionText = createCostFunctionText(flowEdge, lineGroup);
                Text flowText = createFlowText(flowEdge, lineGroup);

                Line flowFillLine = (Line) lineGroup.lookup("#FlowFill");
                Color fillColor = calculateColorBasedOnFlow(flowEdge.getCurrentFlow());
                flowFillLine.setStroke(fillColor);

                // 3. Add Elements to Group
                edgeGroup.getChildren().addAll(costFunctionText, flowText);
            }

            // 4. Adjust the edgeGroup
            adjustEdgeGroup(edgeGroup, edge);

            edgeGroup.setOpacity(0.9);

            edgeGraphicsMap.put(edge, edgeGroup);
            edgeLayer.getChildren().add(edgeGroup);
        } else {
            // Update existing edge position
            updateEdgePosition(edge, edgeGroup);
        }
    }

    private Group createLineElements(Edge edge) {
        Group lineGroup = new Group();
        lineGroup.setId("LineGroup");

        double length = Math.sqrt(Math.pow(edge.getTo().getX() - edge.getFrom().getX(), 2) + Math.pow(edge.getTo().getY() - edge.getFrom().getY(), 2));

        Line line1 = new Line(0, 0, length, 0);
        Line line2 = new Line(0, 0, length, 0);
        Line middleLine = new Line(0, 0, length, 0);
        middleLine.setId("FlowFill");

        double fillStroke = 4.0;
        double borderStroke = 1.5;
        double offset = fillStroke - (borderStroke / 2);
        adjustLinePosition(line1, offset);
        adjustLinePosition(line2, -offset);
        line1.setStrokeWidth(borderStroke);
        line2.setStrokeWidth(borderStroke);

        Color fillColor = calculateColorBasedOnFlow(0);
        middleLine.setStroke(fillColor);
        middleLine.setStrokeWidth(fillStroke);

        lineGroup.getChildren().addAll(line1, line2, middleLine);
        return lineGroup;
    }

    private Color calculateColorBasedOnFlow(double currentFlow) {
        // Define the range of flow values you want to map to the color gradient
        double minFlow = 0.0;
        double maxFlow = calculateMaxFlow(); // Adjust this value as needed

        // Define the color at the minimum and maximum flow values
        Color minColor = Color.LIGHTYELLOW;
        Color maxColor = Color.RED;

        // Calculate the normalized value of currentFlow within the range [0, 1]
        double normalizedFlow = (currentFlow - minFlow) / (maxFlow - minFlow);

        // Interpolate between minColor and maxColor based on the normalizedFlow value
        double interpolatedRed = minColor.getRed() + normalizedFlow * (maxColor.getRed() - minColor.getRed());
        double interpolatedGreen = minColor.getGreen() + normalizedFlow * (maxColor.getGreen() - minColor.getGreen());
        double interpolatedBlue = minColor.getBlue() + normalizedFlow * (maxColor.getBlue() - minColor.getBlue());

        // Ensure the RGB values are within the valid range [0, 1]
        interpolatedRed = clamp(interpolatedRed, 0.0, 1.0);
        interpolatedGreen = clamp(interpolatedGreen, 0.0, 1.0);
        interpolatedBlue = clamp(interpolatedBlue, 0.0, 1.0);

        // Create and return the interpolated color
        return Color.color(interpolatedRed, interpolatedGreen, interpolatedBlue);
    }

    private double calculateMaxFlow() {
        double maxFlow = 0.0;

        for (Vertex vertex : vertexGraphicsMap.keySet()) {
            if (vertex instanceof FlowVertex) {
                FlowVertex flowVertex = (FlowVertex) vertex;
                maxFlow += flowVertex.getFlowCapacity();
            }
        }

        return maxFlow;
    }

    private double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    private Node createCostFunctionText(FlowEdge flowEdge, Node node) {
        Text costFunctionText = new Text("c(x) = " + flowEdge.getCostFunction());
        costFunctionText.setId("CostFunctionText");

        costFunctionText.setFont(Font.font("Gill Sans MT Bold", FontWeight.BOLD, 14));
        costFunctionText.setFill(Color.BLACK);
        costFunctionText.setOpacity(0.8);

        double offset = node.getLayoutBounds().getHeight() + costFunctionText.getLayoutBounds().getHeight() + 5.0;
        costFunctionText.setX(node.getLayoutBounds().getWidth() / 2 - costFunctionText.getLayoutBounds().getWidth() / 2);
        costFunctionText.setY(node.getLayoutBounds().getHeight() / 2 - costFunctionText.getLayoutBounds().getHeight() + offset);

        costFunctionText.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                // Handle double-click event
                handleCostFunctionTextDoubleClick(flowEdge);
            }
        });

        return costFunctionText;
    }

    private void handleCostFunctionTextDoubleClick(FlowEdge flowEdge) {
        // Open a dialog or text input to update the cost function
        TextInputDialog dialog = new TextInputDialog(flowEdge.getCostFunction());
        dialog.setTitle("Edit Cost Function");
        dialog.setHeaderText("Update the Cost Function");
        dialog.setContentText("Enter new cost function:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newCostFunction -> {
            flowEdge.setCostFunction(newCostFunction);
        });
    }

    private Text createFlowText(FlowEdge flowEdge, Node node) {
        Text flowText = new Text(String.valueOf(flowEdge.getCurrentFlow()));
        flowText.setId("FlowText");

        flowText.setFont(Font.font("Gill Sans MT Bold", FontWeight.BOLD, 10));
        flowText.setFill(Color.RED);

        // stinks
        double offset = -(node.getLayoutBounds().getHeight() - flowText.getLayoutBounds().getHeight() + 5.0);
        flowText.setX(node.getLayoutBounds().getWidth() / 2 - flowText.getLayoutBounds().getWidth() / 2);
        flowText.setY(node.getLayoutBounds().getHeight() / 2 - flowText.getLayoutBounds().getHeight() + offset);

        return flowText;
    }

    private void adjustLinePosition(Line line, double offset) {
        // Simplified af
        line.setStartY(line.getStartY() + offset);
        line.setEndY(line.getEndY() + offset);
    }

    private void updateEdgePosition(Edge edge, Group edgeGroup) {
//        edgeGroup.setStartX(edge.getFrom().getX());
//        edgeGroup.setStartY(edge.getFrom().getY());
//        edgeGroup.setEndX(edge.getTo().getX());
//        edgeGroup.setEndY(edge.getTo().getY());
    }

    private void adjustEdgeGroup(Group edgeGroup, Edge edge) {
        // Calculate the angle of rotation
        double angle = calculateEdgeAngle(edge);

        // Determine start position based on the angle
        double startX, startY;
        if (angle > Math.PI / 2 || angle < -Math.PI / 2) {
            // Start from edge's end vertex
            startX = edge.getTo().getX();
            startY = edge.getTo().getY();
            angle -= Math.PI;
        } else {
            // Start from edge's start vertex
            startX = edge.getFrom().getX();
            startY = edge.getFrom().getY();
        }

        // Set the rotation and position of the group
        edgeGroup.setLayoutX(startX);
        edgeGroup.setLayoutY(startY);

        Rotate rotate = new Rotate(Math.toDegrees(angle), 0, 0);
        edgeGroup.getTransforms().add(rotate);
    }

    private double calculateEdgeAngle(Edge edge) {
        double dx = edge.getTo().getX() - edge.getFrom().getX();
        double dy = edge.getTo().getY() - edge.getFrom().getY();
        double angle = Math.atan2(dy, dx);

        return angle;
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

    public void undrawVertexOnly(Vertex vertex) {
        Group vertexGroup = vertexGraphicsMap.get(vertex);
        if (vertexGroup != null) {
            vertexLayer.getChildren().remove(vertexGroup); // Remove the vertex from the vertex layer
            vertexGraphicsMap.remove(vertex); // Remove the vertex from the map
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

    @Override
    public void update(Object subject) {
        if (subject instanceof FlowEdge) {
            undrawEdge((FlowEdge)subject);
            drawEdge((FlowEdge)subject);
        }
        else if (subject instanceof FlowVertex) {
            undrawVertexOnly((FlowVertex)subject);
            drawVertex((FlowVertex)subject);

            // We've changed the Source vertex
            // Run the simulation or
            if (((FlowVertex) subject).isSource()) {

            }
        }
        // Handle other types of subjects...
    }
}


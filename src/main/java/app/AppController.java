package app;

import app.GameObjects.Path;
import app.managers.graph.GraphManager;
import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
import app.managers.graph.flow.FlowEdge;
import app.managers.graph.flow.FlowPath;
import app.managers.graph.flow.FlowVertex;
import app.managers.view.GraphViewManager;
import app.managers.action.GraphActionManager;
import app.managers.view.common.SelectionResult;
import app.managers.view.common.SelectionType;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

import java.util.*;

public class AppController {
    private static AppController instance;
    private AppState state;
    private GraphManager graphManager;
    private GraphViewManager viewManager;
    private GraphActionManager actionManager;
    private Map<Integer, Vertex> printedVertices;
    private double initialX;
    private double initialY;
    private boolean ctrlKeyPressed = false;

    private AppController(Scene scene) {
        this.state = new AppState();
        this.graphManager = new GraphManager();
        this.viewManager = new GraphViewManager(scene);

        this.actionManager = new GraphActionManager(graphManager, viewManager, state);
        this.printedVertices = new HashMap<>();

        initializeEventHandlers();
    }

    private void initializeEventHandlers() {
        Pane graphView = viewManager.getGraphView();

        graphView.setOnMouseClicked(this::handleMouseClicked);
        graphView.setOnMousePressed(this::handleMousePressed);
        graphView.setOnMouseReleased(this::handleMouseReleased);
        graphView.setOnMouseDragged(this::handleMouseDragged);
        graphView.setOnScroll(this::handleScroll);

        graphView.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        graphView.getScene().addEventFilter(KeyEvent.KEY_RELEASED, this::handleKeyReleased);
    }

    public void drawGraph(List<Path> paths, int verticesNum) {
        double width = viewManager.getGraphView().getWidth();
        double height = viewManager.getGraphView().getHeight();
        int pathNum = paths.size();
        int maxPathLength = 0;
        for (Path path : paths) {
            maxPathLength = Math.max(maxPathLength, path.routeLength());
        }
        int xStart = 0;
        int yStart = (int) (pathNum / 2);
        int xEnd = maxPathLength - 1;
        int yEnd = yStart;
        double cellHeight = height / pathNum;
        double cellWidth = width / maxPathLength;
        addVertexFromPath(0, getVertexInGraph(cellWidth, cellHeight, xStart, yStart));
        for (int i = 0; i < paths.size(); i++) {
            Path p = paths.get(i);
            for (int j = 1; j < p.routeLength() - 1; j++) {
                int id = p.getRoute().get(j);
                if (!wasPrinted(id)) {
                    addVertexFromPath(id, getVertexInGraph(cellWidth, cellHeight, j, i));
                }
                actionManager.addEdge(printedVertices.get(p.getRoute().get(j - 1)), printedVertices.get(p.getRoute().get(j)));
            }
        }
        addVertexFromPath(verticesNum - 1, getVertexInGraph(cellWidth, cellHeight, xEnd, yEnd));
        //add last edges
        for (Path path : paths) {
            int lastIndex = path.getRoute().get(path.getRoute().size() - 2);
            FlowEdge newEdge = new FlowEdge(printedVertices.get(lastIndex), printedVertices.get(verticesNum - 1), "1");
            actionManager.addEdge(newEdge);
        }
    }

    private boolean wasPrinted(int id) {
        for (Integer printedId : printedVertices.keySet()) {
            if (printedId == id) {
                return true;
            }
        }
        return false;
    }

    private void addVertexFromPath(int id, Vertex vertex) {
        actionManager.addVertex(vertex);
        printedVertices.put(id, vertex);
    }

    private Vertex getVertexInGraph(double cellWidth, double cellHeight, int x, int y) {
        double offset = x % 2 == 0 ? 0.5 : 0.75;
        return new FlowVertex(cellWidth * (x + offset), cellHeight * (y + offset));
    }

    private void handleMouseClicked(MouseEvent event) {
    }

    private void handleClickSelection(double x, double y, double selectionThreshold) {
        SelectionResult selection = viewManager.selectElementAt(x, y, selectionThreshold);

        if (selection.getType() == SelectionType.VERTEX) {
            Vertex selectedVertex = (Vertex) selection.getSelectedObject();
            state.setSelectedVertex(selectedVertex);
            actionManager.selectVertex(selectedVertex);
        } else if (selection.getType() == SelectionType.EDGE) {
            Edge selectedEdge = (Edge) selection.getSelectedObject();
            state.setSelectedEdge(selectedEdge);
            actionManager.selectEdge(selectedEdge);
        }

        state.setSelectionType(selection.getType());
    }

    private void handleMousePressed(MouseEvent event) {
        Pane vertexView = viewManager.getVertexLayer();
        double adjustedX = getAdjustedX(event, vertexView);
        double adjustedY = getAdjustedY(event, vertexView);

        // Drag
        if (event.isControlDown() && event.isPrimaryButtonDown()) {
            initialX = event.getSceneX();
            initialY = event.getSceneY();
            return;
        }

        // Vertex adding / selection
        if (event.getButton() == MouseButton.SECONDARY && state.getSelectionType() == SelectionType.NONE) {
            FlowVertex newVertex = new FlowVertex(adjustedX, adjustedY);
            newVertex.addObserver(viewManager);
            actionManager.addVertex(newVertex);
        } else if (event.getButton() == MouseButton.PRIMARY) {
            // Edge / Vertex selection
            if (event.isShiftDown()) {
                double SELECTION_THRESHOLD = 30.0;
                handleClickSelection(adjustedX, adjustedY, SELECTION_THRESHOLD);
                return;
            } else if (event.getClickCount() >= 2) {
                double SELECTION_THRESHOLD = 80.0; // bigger for double click
                handleClickSelection(adjustedX, adjustedY, SELECTION_THRESHOLD);
                return;
            }
        }

        // Selection
        if (event.getButton() == MouseButton.PRIMARY) {
            double SELECTION_THRESHOLD = 15.0;
            SelectionResult selection = viewManager.selectElementAt(adjustedX, adjustedY, SELECTION_THRESHOLD);

            if (selection.getType() == SelectionType.VERTEX) {
                Vertex selectedVertex = (Vertex) selection.getSelectedObject();
                state.setSelectedVertex(selectedVertex);
                viewManager.startDrawingEdge(adjustedX, adjustedY);
                // You might want to visually indicate that this vertex is selected for edge creation
                viewManager.highlightVertex(selectedVertex);
            }

            if (state.getSelectedVertex() != null) {
                viewManager.updateDrawingEdge(adjustedX, adjustedY);
            }
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        Pane vertexView = viewManager.getVertexLayer();
        double adjustedX = getAdjustedX(event, vertexView);
        double adjustedY = getAdjustedY(event, vertexView);

        viewManager.stopDrawingEdge();

        if (event.getButton() == MouseButton.PRIMARY && event.isShiftDown()) {
            return;
        }

        if (event.getButton() == MouseButton.PRIMARY && state.getSelectedVertex() != null) {
            double SELECTION_THRESHOLD = 20.0;
            SelectionResult selection = viewManager.selectElementAt(adjustedX, adjustedY, SELECTION_THRESHOLD);

            if (selection.getType() == SelectionType.VERTEX) {
                Vertex endingVertex = (Vertex) selection.getSelectedObject();
                if (!endingVertex.equals(state.getSelectedVertex())) {
                    FlowEdge newEdge = new FlowEdge(state.getSelectedVertex(), endingVertex, "1");
                    newEdge.addObserver(viewManager);
                    if (!graphManager.isEdgePresent(newEdge)) {
                        actionManager.addEdge(newEdge);
                    }
                }
            }
            resetSelection();
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        Pane vertexView = viewManager.getVertexLayer();
        double adjustedX = getAdjustedX(event, vertexView);
        double adjustedY = getAdjustedY(event, vertexView);

        if (state.getSelectedVertex() != null) {
            viewManager.updateDrawingEdge(adjustedX, adjustedY);
        }

        if (event.isControlDown() && event.getButton() == MouseButton.PRIMARY) {
            double deltaX = event.getSceneX() - initialX;
            double deltaY = event.getSceneY() - initialY;

            // Update the graph view position
            Pane vertexLayer = viewManager.getVertexLayer();
            vertexLayer.setTranslateX(vertexLayer.getTranslateX() + deltaX);
            vertexLayer.setTranslateY(vertexLayer.getTranslateY() + deltaY);

            Pane edgeLayer = viewManager.getEdgeLayer();
            edgeLayer.setTranslateX(edgeLayer.getTranslateX() + deltaX);
            edgeLayer.setTranslateY(edgeLayer.getTranslateY() + deltaY);

            initialX = event.getSceneX();
            initialY = event.getSceneY();
        }
    }

    private void handleScroll(ScrollEvent event) {
//        Pane vertexView = viewManager.getVertexLayer();
//        Pane edgeView = viewManager.getEdgeLayer();
//
//        double adjustedX = getAdjustedX(event, vertexView);
//        double adjustedY = getAdjustedY(event, vertexView);
//
//        double zoomFactor = 1.05;
//        double deltaY = event.getDeltaY();
//
//        if (deltaY < 0) {
//            // Scrolling down, zoom out
//            zoomFactor = 1 / zoomFactor;
//        }
//
//        // Set zoom for vertex layer
//        vertexView.setScaleX(vertexView.getScaleX() * zoomFactor);
//        vertexView.setScaleY(vertexView.getScaleY() * zoomFactor);
//
//        // Set zoom for edge layer
//        edgeView.setScaleX(edgeView.getScaleX() * zoomFactor);
//        edgeView.setScaleY(edgeView.getScaleY() * zoomFactor);
//
//        event.consume();
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            if (state.getSelectionType() == SelectionType.VERTEX) {
                actionManager.removeVertex(state.getSelectedVertex());
                resetSelection();
            } else if (state.getSelectionType() == SelectionType.EDGE) {
                actionManager.removeEdge(state.getSelectedEdge());
                resetSelection();
            }
        } else if (event.getCode() == KeyCode.ESCAPE) {
            resetSelection();
        } else if (event.isControlDown() && event.getCode() == KeyCode.Z) {
            actionManager.undo();
        } else if (event.isControlDown() && event.getCode() == KeyCode.Y) {
            actionManager.redo();
        }

        if (event.getCode() == KeyCode.CONTROL) {
            ctrlKeyPressed = true;
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.CONTROL) {
            ctrlKeyPressed = false;
        }
    }

    private void resetSelection() {
        state = new AppState();
        viewManager.resetVertexHighlight();
        viewManager.resetEdgeHighlight();
    }

    public void clearGraph() {
        actionManager.clearGraph();
        resetSelection();
    }

    private double getAdjustedX(MouseEvent event, Pane zoomedPane) {
        // Convert the event's scene coordinates to the coordinates in the pane's parent
        Point2D pointInScene = new Point2D(event.getSceneX(), event.getSceneY());
        Point2D pointInPaneParent = zoomedPane.getParent().sceneToLocal(pointInScene);

        // Adjust for the zooming and translation applied to the zoomedPane
        double adjustedX = (pointInPaneParent.getX() - zoomedPane.getTranslateX()) / zoomedPane.getScaleX();
        return adjustedX;
    }

    private double getAdjustedY(MouseEvent event, Pane zoomedPane) {
        // Convert the event's scene coordinates to the coordinates in the pane's parent
        Point2D pointInScene = new Point2D(event.getSceneX(), event.getSceneY());
        Point2D pointInPaneParent = zoomedPane.getParent().sceneToLocal(pointInScene);

        // Adjust for the zooming and translation applied to the zoomedPane
        double adjustedY = (pointInPaneParent.getY() - zoomedPane.getTranslateY()) / zoomedPane.getScaleY();
        return adjustedY;
    }

    private double getAdjustedX(ScrollEvent event, Pane view) {
        double zoomScale = view.getScaleX(); // Assuming uniform scaling for X and Y
        double adjustedX = (event.getX() - view.getTranslateX()) / zoomScale;
        return adjustedX;
    }

    private double getAdjustedY(ScrollEvent event, Pane view) {
        double zoomScale = view.getScaleY(); // Assuming uniform scaling for X and Y
        double adjustedY = (event.getY() - view.getTranslateY()) / zoomScale;
        return adjustedY;
    }

    public void returnHome() {
        Pane vertexView = viewManager.getVertexLayer();
        Pane edgeView = viewManager.getEdgeLayer();

        // Reset the translation to the initial position (usually 0,0)
        vertexView.setTranslateX(0);
        vertexView.setTranslateY(0);
        edgeView.setTranslateX(0);
        edgeView.setTranslateY(0);
    }

    public void runSimulationOptimum() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Perform long-running task here
                viewManager.setCostOptimal(0.0);

                System.out.println("Running simulation for Optimum Flow");
                final double cost = getTotalCost(true);

                viewManager.setCostOptimal(cost);
            }
        }).start();
    }

    public void runSimulationNashEquilibrium() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Perform long-running task here
                viewManager.setCostNashEquilibrium(0.0);

                System.out.println("Running simulation for Nash Flow");
                final double cost = getTotalCost(false);

                viewManager.setCostNashEquilibrium(cost);
            }
        }).start();
    }

    double getTotalCost(boolean isGoalOptimum) {
        List<FlowPath> paths = graphManager.getGraph().findAllPaths();
        if (paths.size() == 0) {
            System.out.println("No paths found! Please ensure that the graph contains a source and a sink vertex");
            return 0.0;
        }

        if (paths.size() == 1) {
            System.out.println("Only one path found!");
            return 0.0;
        }

        double totalFlow = viewManager.calculateMaxFlow();

        MultivariateFunction function = objectiveFunction(
                totalFlow,
                paths,
                viewManager.isOptionDiscrete(),
                isGoalOptimum);

        // Optimizer
        int numberOfPoints = 2 * (paths.size()  - 1) + 1;
        if (numberOfPoints < 2) {
            numberOfPoints = 2;
        }

        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(numberOfPoints); // Number of interpolation points

        // Optimize the objective function
        double[] lowerBound = new double[paths.size() - 1]; // Lower bounds (all zeros)
        double[] upperBound = new double[paths.size() - 1]; // Upper bounds (optional, can be set to a large number or left unbounded)
        Arrays.fill(lowerBound, 0.0);
        Arrays.fill(upperBound, Double.POSITIVE_INFINITY);

        double[] initialGuess = new double[paths.size() - 1];
        for (int i = 0; i < initialGuess.length; i++) {
            initialGuess[i] = (0 + totalFlow) / (initialGuess.length + 1);
        }

        double[] sumCoefficients = new double[paths.size() - 1];
        Arrays.fill(sumCoefficients, 1);
        LinearConstraint sumConstraint = new LinearConstraint(sumCoefficients, Relationship.LEQ, totalFlow);

        PointValuePair optimum = optimizer.optimize(
                new MaxIter(10000),
                new MaxEval(10000),
                new ObjectiveFunction(function),
                GoalType.MINIMIZE,
                new InitialGuess(initialGuess),
                new SimpleBounds(lowerBound, upperBound)
        );

        double[] resultantFlows;
        if (viewManager.isOptionDiscrete()) {
            resultantFlows = calculateIntegerFlowsFromPoint(totalFlow, optimum.getPoint());
        }
        else {
            resultantFlows = calculateFlowsFromPoint(totalFlow, optimum.getPoint());
        }

        double cost = calculateTotalCost(paths, resultantFlows);
        fillGraphFlows(paths, resultantFlows);

        resultantFlows = Arrays.stream(resultantFlows).map(d -> Math.round(d * 100.0) / 100.0).toArray();
        for (int i = 0; i < resultantFlows.length; i++) {
            if (resultantFlows[i] > 0)
                System.out.println("Flow for path " + (i) + " [" + paths.get(i).getCostFunction() + "] : " + "x=" + resultantFlows[i] + " [C(x)= " + paths.get(i).getCurrentCost() + "]");
        }

        return cost;
    }

    private MultivariateFunction objectiveFunction(double T, List<FlowPath> paths, boolean isDiscrete, boolean isGoalOptimum) {

        MultivariateFunction function = point -> {
            // Calculate the flows from the point
            double[] flows;
            if (isDiscrete) {
                flows = calculateIntegerFlowsFromPoint(T, point);
            }
            else {
                flows = calculateFlowsFromPoint(T, point);
            }

            // Calculate the current costs given the flows in the graph
            fillGraphFlows(paths, flows);

            double[] costs = new double[flows.length];
            if (isGoalOptimum) {
                for (int i = 0; i < flows.length; i++) {
                    for (FlowEdge edge : paths.get(i).getEdges()) {
                        costs[i] += edge.getCurrentCost() * flows[i];
                    }
                }
            }
            else {
                for (int i = 0; i < flows.length; i++) {
                    for (FlowEdge edge : paths.get(i).getEdges()) {
                        costs[i] += edge.getCurrentCost();
                    }
                }
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }

            resetGraphFlows(paths);

            // Calculate the objective function - punish for bad point choice, reward for good point choice
            // Objective: Minimize the variance or dissimilarity of costs
            double totalCost = 0;
            if (isGoalOptimum) {
                totalCost = calculateSumObjectiveValue(costs, flows);
            }
            else {
                totalCost = calculateDissimilaritiesObjectiveValue(costs, flows);
            }

            // Apply penalty for breaking the constraints
            double penalty = 0;
            if (Arrays.stream(flows).anyMatch(flow -> flow < 0.0)) {
                penalty += Arrays.stream(flows).filter(flow -> flow < 0.0).map(flow -> Math.abs(flow * flow)).sum(); // Quadratic penalty for negative flows
            }
            double sumFlows = Arrays.stream(flows).sum();
            if (sumFlows > T) {
                penalty += Math.abs((sumFlows - T)*(sumFlows - T)); // Quadratic penalty for exceeding T
            }
            totalCost += penalty;

            return totalCost;
        };

        return function;
    }

    private void fillGraphFlows(List<FlowPath> paths, double[] flows) {
        resetGraphFlows(paths);

        for (int i = 0; i < paths.size(); i++) {
            List<FlowEdge> edges = paths.get(i).getEdges();
            for (FlowEdge edge : edges) {
                edge.setCurrentFlow(edge.getCurrentFlow() + flows[i]);
            }
        }
    }

    private double calculateTotalCost(List<FlowPath> paths, double[] flows) {
        fillGraphFlows(paths, flows);
        double totalFlow = Arrays.stream(flows).sum();

        double result = 0.0;
        for (int i = 0; i < paths.size(); i++) {
            if (totalFlow > 1.0) {
                result += paths.get(i).getCurrentCost() * (flows[i] / totalFlow);
            } else {
                result += paths.get(i).getCurrentCost() * flows[i];
            }
        }

        resetGraphFlows(paths);

        return result;
    }

    private double[] calculateFlowsFromPoint(double totalFlow, double[] point) {
        double[] flows = new double[point.length + 1];
        double remainingFlow = totalFlow;

        for (int i = 0; i < point.length; i++) {
            flows[i] = point[i];
            remainingFlow -= point[i];
        }

        flows[point.length] = remainingFlow;
        return flows;
    }

    private double[] calculateIntegerFlowsFromPoint(double totalFlow, double[] point) {
        double[] flows = new double[point.length + 1];
        int totalIntegerFlow = (int) Math.round(totalFlow);
        int sumOfFlows = 0;

        for (int i = 0; i < point.length; i++) {
            flows[i] = Math.ceil(point[i]);
            sumOfFlows += flows[i];
        }

        // Adjust the last flow to ensure the sum equals totalFlow
        flows[point.length] = totalIntegerFlow - sumOfFlows;

        // Optional: Check if the adjustment made the last flow negative.
        // If so, redistribute the flow.
        if (flows[point.length] < 0) {
            redistributeFlows(flows, totalIntegerFlow);
        }

        return flows;
    }

    private void redistributeFlows(double[] flows, int totalFlow) {
        // Implementation of flow redistribution logic
        // One simple approach could be to reduce other flows by 1 until the sum is balanced.
        // This is a basic and naive approach, more sophisticated logic might be needed based on your specific requirements.
        int sum = Arrays.stream(flows).mapToInt(d -> (int) d).sum();
        for (int i = 0; i < flows.length - 1 && sum > totalFlow; i++) {
            if (flows[i] > 0) {
                flows[i]--;
                sum--;
            }
        }
        flows[flows.length - 1] = totalFlow - sum;
    }

    private void resetGraphFlows(List<FlowPath> paths) {
        double zeroFlow = 0.0;

        for (FlowPath path : paths) {
            for (FlowEdge edge : path.getEdges()) {
                edge.setCurrentFlow(zeroFlow);
            }
        }
    }

    public double calculateSumObjectiveValue(double[] costs, double[] flows) {
        // minimizes the potential function
        double resValue = 0;

        for (int i = 0; i < costs.length; i++) {
            resValue += costs[i];
        }

        return resValue;
    }

    public double calculateDissimilaritiesObjectiveValue(double[] costs, double[] flows) {
        // minimizes the difference between the marginal costs
        double mean = Arrays.stream(costs).average().orElse(0);
        double sumOfSquaredDeviations = Arrays.stream(costs)
                .map(cost -> Math.pow(cost - mean, 2))
                .sum();

        // The objective is to minimize this sum
        return sumOfSquaredDeviations;
    }

    public static AppController getInstance(Scene scene) {
        if (instance == null) {
            instance = new AppController(scene);
        }
        return instance;
    }
    public GraphViewManager getViewManager() {
        return viewManager;
    }
}


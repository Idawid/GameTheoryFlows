package app;

import app.GameObjects.Path;
import app.managers.graph.GraphManager;
import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
import app.managers.graph.flow.FlowEdge;
import app.managers.graph.flow.FlowVertex;
import app.managers.view.GraphViewManager;
import app.managers.action.GraphActionManager;
import app.managers.view.common.SelectionResult;
import app.managers.view.common.SelectionType;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppController {
    private AppState state;
    private GraphManager graphManager;
    private GraphViewManager viewManager;
    private GraphActionManager actionManager;
    private Map<Integer, Vertex> printedVertices;

    public AppController(Scene scene) {
        this.state = new AppState();
        this.graphManager = new GraphManager();
        this.viewManager = new GraphViewManager(scene);
        this.actionManager = new GraphActionManager(graphManager, viewManager, state);
        this.printedVertices = new HashMap<>();
        initializeEventHandlers(scene);
    }

    private void initializeEventHandlers(Scene scene) {
        Pane graphView = viewManager.getGraphView();

        graphView.setOnMouseClicked(this::handleMouseClicked);
        graphView.setOnMousePressed(this::handleMousePressed);
        graphView.setOnMouseReleased(this::handleMouseReleased);
        graphView.setOnMouseDragged(this::handleMouseDragged);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
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
        if (event.getButton() == MouseButton.SECONDARY && state.getSelectionType() == SelectionType.NONE) {
            FlowVertex newVertex = new FlowVertex(event.getX(), event.getY());
            newVertex.addObserver(viewManager);
            actionManager.addVertex(newVertex);
        } else if (event.getButton() == MouseButton.PRIMARY) {
            // Edge / Vertex selection
            if (event.isShiftDown()) {
                double SELECTION_THRESHOLD = 30.0;
                handleClickSelection(event.getX(), event.getY(), SELECTION_THRESHOLD);
            } else if (event.getClickCount() >= 2) {
                double SELECTION_THRESHOLD = 80.0; // bigger for double click
                handleClickSelection(event.getX(), event.getY(), SELECTION_THRESHOLD);
            }
        }
    }

    private void handleClickSelection(double x, double y, double selectionThreshold) {
        SelectionResult selection = viewManager.selectElementAt(x, y, selectionThreshold);

        if (selection.getType() == SelectionType.VERTEX) {
            Vertex selectedVertex = (Vertex) selection.getSelectedObject();
            actionManager.selectVertex(selectedVertex);
            state.setSelectedVertex(selectedVertex);
        } else if (selection.getType() == SelectionType.EDGE) {
            Edge selectedEdge = (Edge) selection.getSelectedObject();
            actionManager.selectEdge(selectedEdge);
            state.setSelectedEdge(selectedEdge);
        }

        state.setSelectionType(selection.getType());
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            double SELECTION_THRESHOLD = 15.0;
            SelectionResult selection = viewManager.selectElementAt(event.getX(), event.getY(), SELECTION_THRESHOLD);

            if (selection.getType() == SelectionType.VERTEX) {
                Vertex selectedVertex = (Vertex) selection.getSelectedObject();
                state.setSelectedVertex(selectedVertex);
                viewManager.startDrawingEdge(selectedVertex.getX(), selectedVertex.getY());
                // You might want to visually indicate that this vertex is selected for edge creation
                viewManager.highlightVertex(selectedVertex);
            }

            if (state.getSelectedVertex() != null) {
                viewManager.updateDrawingEdge(event.getX(), event.getY());
            }
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        viewManager.stopDrawingEdge();

        if (event.getButton() == MouseButton.PRIMARY && state.getSelectedVertex() != null) {
            double SELECTION_THRESHOLD = 20.0;
            SelectionResult selection = viewManager.selectElementAt(event.getX(), event.getY(), SELECTION_THRESHOLD);

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
        if (state.getSelectedVertex() != null) {
            viewManager.updateDrawingEdge(event.getX(), event.getY());
        }
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
    }

    private void resetSelection() {
        state = new AppState();
        viewManager.resetVertexSelection();
        viewManager.resetEdgeHighlight();
    }
}

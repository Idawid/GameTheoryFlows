package app;

import app.managers.action.common.SelectEdgeAction;
import app.managers.action.common.SelectVertexAction;
import app.managers.graph.GraphManager;
import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
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

public class AppController {
    private AppState state;
    private GraphManager graphManager;
    private GraphViewManager viewManager;
    private GraphActionManager actionManager;
    public AppController(Scene scene) {
        this.state = new AppState();
        this.graphManager = new GraphManager();
        this.viewManager = new GraphViewManager(scene);
        this.actionManager = new GraphActionManager(graphManager, viewManager, state);

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

    private void handleMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY && state.getSelectionType() == SelectionType.NONE) {
            Vertex newVertex = new Vertex(event.getX(), event.getY());
            actionManager.addVertex(newVertex);
        }
        else if (event.getButton() == MouseButton.PRIMARY) {
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
                    Edge newEdge = new Edge(state.getSelectedVertex(), endingVertex);
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

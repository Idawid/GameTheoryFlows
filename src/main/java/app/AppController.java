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

        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    private void handleMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY && state.getSelectionType() == SelectionType.NONE) {
            Vertex newVertex = new Vertex(event.getX(), event.getY());
            actionManager.addVertex(newVertex);
        } else if (event.getButton() == MouseButton.PRIMARY && event.isShiftDown()) {
            double SELECTION_THRESHOLD = 10.0;
            SelectionResult selection = viewManager.selectElementAt(event.getX(), event.getY(), SELECTION_THRESHOLD);

            if (selection.getType() == SelectionType.VERTEX) {
                Vertex selectedVertex = (Vertex) selection.getSelectedObject();
                SelectVertexAction selectVertexAction = new SelectVertexAction(selectedVertex, graphManager, viewManager);
                actionManager.performAction(selectVertexAction);
            } else if (selection.getType() == SelectionType.EDGE) {
                Edge selectedEdge = (Edge) selection.getSelectedObject();
                SelectEdgeAction selectEdgeAction = new SelectEdgeAction(selectedEdge, graphManager, viewManager);
                actionManager.performAction(selectEdgeAction);
            }

            state.setSelectionType(selection.getType());
        }
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY && state.getSelectionType() == SelectionType.VERTEX) {
            // Start edge drawing process
            // startEdgeDrawing(state.getSelectedVertex());
        }
    }

    private void handleMouseReleased(MouseEvent event) {
//        if (state.isDrawingEdge()) {
//            // Finish edge drawing process
//            // finishEdgeDrawing(event.getX(), event.getY());
//        }
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            if (state.getSelectionType() == SelectionType.VERTEX) {
                actionManager.removeVertex(state.getSelectedVertex());
            } else if (state.getSelectionType() == SelectionType.EDGE) {
                actionManager.removeEdge(state.getSelectedEdge());
            }
        } else if (event.getCode() == KeyCode.ESCAPE) {
            state = new AppState();
            viewManager.resetVertexSelection();
            viewManager.resetEdgeHighlight();
        } else if (event.isControlDown() && event.getCode() == KeyCode.Z) {
            actionManager.undo();
        } else if (event.isControlDown() && event.getCode() == KeyCode.Y) {
            actionManager.redo();
        }
    }
}

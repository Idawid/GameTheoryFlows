package app.managers.action.common;

import app.managers.graph.common.Edge;
import app.managers.graph.GraphManager;
import app.managers.view.GraphViewManager;
import javafx.scene.Group;
import javafx.scene.shape.Line;

public class RemoveEdgeAction implements Action {
    private Edge edge;
    private GraphManager graphManager;
    private GraphViewManager viewManager;
    private Group edgeLine; // To keep a reference for undo

    public RemoveEdgeAction(Edge edge, GraphManager graphManager, GraphViewManager viewManager) {
        this.edge = edge;
        this.graphManager = graphManager;
        this.viewManager = viewManager;
        this.edgeLine = viewManager.getEdgeGraphicsMap().get(edge);
    }

    @Override
    public void perform() {
        graphManager.removeEdge(edge);
        // Update the view to remove the edge
        if (edgeLine != null) {
            viewManager.getEdgeLayer().getChildren().remove(edgeLine);
        }
    }

    @Override
    public void undo() {
        graphManager.addEdge(edge);
        // Update the view to restore the edge
        if (edgeLine != null) {
            viewManager.getEdgeLayer().getChildren().add(edgeLine);
        }
    }
}


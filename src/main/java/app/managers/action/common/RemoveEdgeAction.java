package app.managers.action.common;

import app.managers.graph.common.Edge;
import app.managers.graph.GraphManager;
import app.managers.view.GraphViewManager;
import javafx.scene.Group;

public class RemoveEdgeAction implements Action {
    private Edge edge;
    private GraphManager graphManager;
    private GraphViewManager viewManager;
    private Group edgeGroup; // To keep a reference for undo

    public RemoveEdgeAction(Edge edge, GraphManager graphManager, GraphViewManager viewManager) {
        this.edge = edge;
        this.graphManager = graphManager;
        this.viewManager = viewManager;
        this.edgeGroup = viewManager.getEdgeGraphicsMap().get(edge);
    }

    @Override
    public void perform() {
        graphManager.removeEdge(edge);
        viewManager.undrawEdge(edge);
    }

    @Override
    public void undo() {
        graphManager.addEdge(edge);
        viewManager.drawEdge(edge);
    }
}


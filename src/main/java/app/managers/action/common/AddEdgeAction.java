package app.managers.action.common;

import app.managers.graph.GraphManager;
import app.managers.graph.common.Edge;
import app.managers.view.GraphViewManager;
import javafx.scene.Group;
import javafx.scene.shape.Line;

public class AddEdgeAction implements Action {
    private Edge edge;
    private GraphManager graphManager;
    private GraphViewManager viewManager;

    public AddEdgeAction(Edge edge, GraphManager graphManager, GraphViewManager viewManager) {
        this.edge = edge;
        this.graphManager = graphManager;
        this.viewManager = viewManager;
    }

    @Override
    public void perform() {
        graphManager.addEdge(edge);
        viewManager.drawEdge(edge);
    }

    @Override
    public void undo() {
        graphManager.removeEdge(edge);
        viewManager.undrawEdge(edge);
    }
}


package app.managers.action.common;

import app.managers.graph.common.Edge;
import app.managers.graph.GraphManager;
import app.managers.view.GraphViewManager;

public class SelectEdgeAction implements Action {
    private Edge edge;
    private GraphManager graphManager;
    private GraphViewManager viewManager;

    public SelectEdgeAction(Edge edge, GraphManager graphManager, GraphViewManager viewManager) {
        this.edge = edge;
        this.graphManager = graphManager;
        this.viewManager = viewManager;
    }

    @Override
    public void perform() {
        viewManager.highlightEdge(edge);
    }

    @Override
    public void undo() {
        viewManager.resetEdgeHighlight();
    }
}

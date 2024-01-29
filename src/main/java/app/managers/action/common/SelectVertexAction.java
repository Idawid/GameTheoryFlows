package app.managers.action.common;

import app.managers.graph.GraphManager;
import app.managers.view.GraphViewManager;
import app.managers.graph.common.Vertex;

public class SelectVertexAction implements Action {
    private Vertex vertex;
    private GraphManager graphManager;
    private GraphViewManager viewManager;

    public SelectVertexAction(Vertex vertex, GraphManager graphManager, GraphViewManager viewManager) {
        this.vertex = vertex;
        this.graphManager = graphManager;
        this.viewManager = viewManager;
    }

    @Override
    public void perform() {
        viewManager.highlightVertex(vertex);
    }

    @Override
    public void undo() {
        viewManager.resetVertexHighlight();
    }
}


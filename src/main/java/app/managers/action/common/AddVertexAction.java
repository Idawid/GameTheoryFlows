package app.managers.action.common;

import app.managers.graph.GraphManager;
import app.managers.view.GraphViewManager;
import app.managers.graph.common.Vertex;
import javafx.scene.Group;

public class AddVertexAction implements Action {
    private Vertex vertex;
    private GraphManager graphManager;
    private GraphViewManager viewManager;

    public AddVertexAction(Vertex vertex, GraphManager graphManager, GraphViewManager viewManager) {
        this.vertex = vertex;
        this.graphManager = graphManager;
        this.viewManager = viewManager;
    }

    @Override
    public void perform() {
        graphManager.addVertex(vertex);
        viewManager.drawVertex(vertex);
    }

    @Override
    public void undo() {
        graphManager.removeVertex(vertex);
        Group vertexGroup = viewManager.getVertexGraphicsMap().get(vertex);
        if (vertexGroup != null) {
            viewManager.getGraphView().getChildren().remove(vertexGroup);
            viewManager.getVertexGraphicsMap().remove(vertex);
        }
    }
}


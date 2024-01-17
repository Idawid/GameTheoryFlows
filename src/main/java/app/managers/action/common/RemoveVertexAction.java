package app.managers.action.common;

import app.managers.graph.GraphManager;
import app.managers.view.GraphViewManager;
import app.managers.graph.common.Vertex;
import javafx.scene.Group;

public class RemoveVertexAction implements Action {
    private Vertex vertex;
    private GraphManager graphManager;
    private GraphViewManager viewManager;
    private Group vertexGroup; // To keep a reference for undo

    public RemoveVertexAction(Vertex vertex, GraphManager graphManager, GraphViewManager viewManager) {
        this.vertex = vertex;
        this.graphManager = graphManager;
        this.viewManager = viewManager;
        this.vertexGroup = viewManager.getVertexGraphicsMap().get(vertex);
    }

    @Override
    public void perform() {
        graphManager.removeVertex(vertex);
        // Update the view to remove the vertex and its edges
        if (vertexGroup != null) {
            viewManager.getVertexLayer().getChildren().remove(vertexGroup);
        }
    }

    @Override
    public void undo() {
        graphManager.addVertex(vertex);
        // Update the view to restore the vertex and its edges
        if (vertexGroup != null) {
            viewManager.getVertexLayer().getChildren().add(vertexGroup);
        }
    }
}




package app.managers.action.common;

import app.managers.graph.GraphManager;
import app.managers.graph.common.Edge;
import app.managers.view.GraphViewManager;
import app.managers.graph.common.Vertex;
import javafx.scene.Group;

import java.util.ArrayList;
import java.util.List;

public class RemoveVertexAction implements Action {
    private Vertex vertex;
    private GraphManager graphManager;
    private GraphViewManager viewManager;
    private List<Edge> connectedEdges;

    public RemoveVertexAction(Vertex vertex, GraphManager graphManager, GraphViewManager viewManager) {
        this.vertex = vertex;
        this.graphManager = graphManager;
        this.viewManager = viewManager;
        this.connectedEdges = viewManager.getConnectedEdges(vertex); // Store connected edges
    }

    @Override
    public void perform() {
        graphManager.removeVertex(vertex);
        viewManager.undrawVertex(vertex);
    }

    @Override
    public void undo() {
        graphManager.addVertex(vertex);
        viewManager.drawVertex(vertex);
        connectedEdges.forEach(viewManager::drawEdge);
    }
}




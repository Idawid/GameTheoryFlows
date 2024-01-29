package app.managers.action.common;

import app.managers.graph.GraphManager;
import app.managers.graph.common.Edge;
import app.managers.view.GraphViewManager;
import app.managers.graph.common.Vertex;

import java.util.ArrayList;
import java.util.List;

public class ClearGraphAction implements Action {
    private GraphManager graphManager;
    private GraphViewManager viewManager;
    private List<Vertex> deletedVertices;
    private List<Edge> deletedEdges;
    private Vertex highlightedVertex;
    private Edge highlightedEdge;

    public ClearGraphAction(GraphManager graphManager, GraphViewManager viewManager, Vertex highlightedVertex, Edge highlightedEdge) {
        this.graphManager = graphManager;
        this.viewManager = viewManager;
        this.deletedVertices = new ArrayList<>();
        this.deletedEdges = new ArrayList<>();
        this.highlightedVertex = highlightedVertex;
        this.highlightedEdge = highlightedEdge;
    }

    @Override
    public void perform() {
        // Store the vertices before deleting them
        deletedVertices = new ArrayList<>(viewManager.getVertexGraphicsMap().keySet());
        deletedEdges = new ArrayList<>(viewManager.getEdgeGraphicsMap().keySet());
        for (Vertex vertex : deletedVertices) {
            graphManager.removeVertex(vertex);
            viewManager.undrawVertex(vertex);
        }

        viewManager.resetVertexHighlight();
        viewManager.resetEdgeHighlight();
    }

    @Override
    public void undo() {
        // Re-add the stored vertices
        for (Vertex vertex : deletedVertices) {
            graphManager.addVertex(vertex);
            viewManager.drawVertex(vertex); // Assuming there is a method to redraw a vertex
        }
        for (Edge edge : deletedEdges) {
            graphManager.addEdge(edge);
            viewManager.drawEdge(edge);
        }
        // Clear the list after undoing
        deletedVertices.clear();
        deletedEdges.clear();

        // Highlight
        // viewManager.highlightVertex(highlightedVertex);
        // viewManager.highlightEdge(highlightedEdge);
    }
}



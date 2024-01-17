package app.managers.graph;

import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
import app.managers.graph.flow.FlowGraph;


public class GraphManager {
    private FlowGraph graph;

    public GraphManager() {
        graph = new FlowGraph();
    }
    public boolean isVertexPresent(Vertex vertex) {
        return graph.isVertexPresent(vertex);
    }

    public boolean isEdgePresent(Edge edge) {
        return graph.isEdgePresent(edge);
    }

    public void addVertex(Vertex vertex) {
        graph.addVertex(vertex);
    }

    public void removeVertex(Vertex vertex) {
        graph.removeVertex(vertex);
    }

    public void addEdge(Edge edge) {
        graph.addEdge(edge);
    }

    public void removeEdge(Edge edge) {
        graph.removeEdge(edge);
    }
}



package app.managers.graph.common;

import java.util.*;

public class Graph {
    protected Map<Integer, Vertex> vertices;
    protected Set<Edge> edges;

    public Graph() {
        this.vertices = new HashMap<>();
        this.edges = new HashSet<>();
    }

    public void addVertex(Vertex vertex) {
        vertices.put(vertex.getId(), vertex);
    }

    public void removeVertex(Vertex vertex) {
        if (vertices.containsKey(vertex.getId())) {
            // Remove the vertex
            vertices.remove(vertex.getId());

            // Remove all connected edges
            edges.removeIf(edge -> edge.getFrom().equals(vertex) || edge.getTo().equals(vertex));
        }
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    public boolean isVertexPresent(Vertex vertex) {
        return vertices.containsKey(vertex.getId());
    }

    public boolean isEdgePresent(Edge edge) {
        return edges.contains(edge);
    }

    // Getters for vertices and edges
    public Map<Integer, Vertex> getVertices() {
        return vertices;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public Vertex getVertex(int vertexId) {
        return vertices.get(vertexId);
    }

    public List<Edge> getConnectedEdges(Vertex vertex) {
        List<Edge> connectedEdges = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getFrom().equals(vertex) || edge.getTo().equals(vertex)) {
                connectedEdges.add(edge);
            }
        }
        return connectedEdges;
    }
}


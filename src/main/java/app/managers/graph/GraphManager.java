package app.managers.graph;

import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;

import java.util.ArrayList;
import java.util.List;

public class GraphManager {
    private List<Vertex> vertices = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    public boolean isVertexPresent(Vertex vertex) {
        for (Vertex v : vertices) {
            if (v.equals(vertex)) {
                return true; // Found a matching vertex
            }
        }
        return false; // Vertex not found
    }

    public boolean isEdgePresent(Edge edge) {
        for (Edge e : edges) {
            if (e.equals(edge)) {
                return true; // Found a matching edge
            }
        }
        return false; // Edge not found
    }

    public void addVertex(Vertex vertex) {
        // Add the vertex to the list of vertices
        vertices.add(vertex);
    }

    public void removeVertex(Vertex vertex) {
        // Remove the vertex from the list of vertices
        vertices.remove(vertex);

        // Remove all edges associated with this vertex
        edges.removeIf(edge -> edge.getFrom().equals(vertex) || edge.getTo().equals(vertex));
    }

    public void addEdge(Edge edge) {
        // Add the edge to the list of edges
        edges.add(edge);
    }

    public void removeEdge(Edge edge) {
        // Remove the edge from the list of edges
        edges.remove(edge);
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }
}



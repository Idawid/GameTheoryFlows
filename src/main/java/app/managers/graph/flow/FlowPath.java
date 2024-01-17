package app.managers.graph.flow;

import app.managers.graph.common.Vertex;

import java.util.ArrayList;
import java.util.List;

public class FlowPath extends FlowEdge {
    private List<FlowEdge> edges;

    public FlowPath() {
        super(null, null, ""); // Placeholder, actual initialization might be different
        this.edges = new ArrayList<>();
    }

    public FlowPath(FlowPath other) {
        super(other.getFrom(), other.getTo(), ""); // Adjust as per actual constructor
        this.edges = new ArrayList<>(other.edges);
    }

    public void addEdge(FlowEdge edge) {
        edges.add(edge);
        // Update the start and end vertices, flow, and cost function as needed
    }

    public void removeLastEdge() {
        if (!edges.isEmpty()) {
            edges.remove(edges.size() - 1);
        }
    }

    @Override
    public Vertex getFrom() {
        if (!edges.isEmpty()) {
            // Return the start vertex from the first edge
            return edges.get(0).getFrom();
        }
        return null; // You can choose the appropriate behavior for an empty path
    }

    @Override
    public Vertex getTo() {
        if (!edges.isEmpty()) {
            // Return the end vertex from the last edge
            return edges.get(edges.size() - 1).getTo();
        }
        return null; // You can choose the appropriate behavior for an empty path
    }

    // Override methods from FlowEdge to represent the combined behavior of all edges in the path
    // For example, current flow might be the sum of flows in all edges, etc.
}

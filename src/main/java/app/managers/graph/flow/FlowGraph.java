package app.managers.graph.flow;


import app.managers.graph.common.Edge;
import app.managers.graph.common.Graph;
import app.managers.graph.common.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FlowGraph extends Graph {
    public FlowGraph() {
        super();
    }

    public List<FlowPath> findAllPaths() {
        List<FlowPath> allPaths = new ArrayList<>();
        List<FlowVertex> sources = findSources();
        List<FlowVertex> sinks = findSinks();

        for (Vertex source : sources) {
            for (Vertex sink : sinks) {
                findAllPathsDFS(source, sink, allPaths, new HashSet<>(), new FlowPath());
            }
        }

        return allPaths;
    }

    private void findAllPathsDFS(Vertex current, Vertex target, List<FlowPath> allPaths, Set<Vertex> visited, FlowPath currentPath) {
        if (visited.contains(current)) {
            return; // Avoid cycles
        }

        if (current.equals(target)) {
            allPaths.add(new FlowPath(currentPath)); // Add a copy of the current path to the list
            return;
        }

        visited.add(current);
        for (Edge edge : getConnectedEdges(current)) {
            if (edge instanceof FlowEdge) {
                currentPath.addEdge((FlowEdge) edge);
                findAllPathsDFS(edge.getTo(), target, allPaths, visited, currentPath);
                currentPath.removeLastEdge(); // Backtrack
            }
        }
        visited.remove(current);
    }

    // Additional functionalities specific to FlowGraph
    public List<FlowVertex> findSources() {
        return vertices.values().stream()
                .filter(v -> v instanceof FlowVertex)
                .map(v -> (FlowVertex) v)
                .filter(FlowVertex::isSource)
                .collect(Collectors.toList());
    }

    public List<FlowVertex> findSinks() {
        return vertices.values().stream()
                .filter(v -> v instanceof FlowVertex)
                .map(v -> (FlowVertex) v)
                .filter(FlowVertex::isSink)
                .collect(Collectors.toList());
    }
}
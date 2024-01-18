package app.managers.graph.flow;

import app.managers.graph.common.Vertex;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.List;

public class FlowPath extends FlowEdge {
    private List<FlowEdge> edges;

    public FlowPath() {
        super(null, null, "0");
        this.edges = new ArrayList<>();
    }

    public FlowPath(FlowPath other) {
        super(null, null, "0");
        this.edges = new ArrayList<>(other.edges);
        updateMembers();
    }

    public void addEdge(FlowEdge edge) {
        edges.add(edge);
        updateMembers();
    }

    public void removeLastEdge() {
        if (!edges.isEmpty()) {
            edges.remove(edges.size() - 1);
        }
        updateMembers();
    }

    private void updateMembers() {
        if (!edges.isEmpty()) {
            this.setFrom(edges.get(0).getFrom());
            this.setTo(edges.get(edges.size() - 1).getTo());

            StringBuilder costFunctionBuilder = new StringBuilder();
            for (FlowEdge edge : edges) {
                costFunctionBuilder.append("(").append(edge.getCostFunction()).append(")+");
            }
            if (!edges.isEmpty()) {
                costFunctionBuilder.deleteCharAt(costFunctionBuilder.length() - 1); // Remove the last '+'
            } else {
                costFunctionBuilder.append("0");
            }
            this.setCostFunction(costFunctionBuilder.toString());
        }
    }

    public List<FlowEdge> getEdges() {
        return edges;
    }
}

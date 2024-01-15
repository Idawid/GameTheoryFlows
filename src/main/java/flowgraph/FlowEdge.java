package flowgraph;

import graph.Edge;
import graph.Vertex;
import math.QuadraticFunction;

public class FlowEdge implements Edge {
    private double flow;
    private Vertex startVertex;
    private Vertex endVertex;
    private QuadraticFunction costFunction;

    @Override
    public Vertex getStartVertex() {
        return startVertex;
    }

    @Override
    public Vertex getEndVertex() {
        return endVertex;
    }

    @Override
    public double getCost() {
        return costFunction.getValue(flow);
    }

    public double getMarginalCost() {
        // (x * c(x))' = x' * c(x) + x * c(x)'
        return 1 * costFunction.getValue(flow) + flow * costFunction.getValue(flow, true);
    }

    @Override
    public int getID() {
        return 0;
    }
}

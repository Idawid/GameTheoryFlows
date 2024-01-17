package app.managers.graph.flow;

import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class FlowEdge extends Edge {
    private double currentFlow;
    private String costFunction; // Mathematical expression as a string
    private transient Expression compiledCostFunction;

    public FlowEdge(Vertex from, Vertex to, String costFunction) {
        super(from, to);
        this.costFunction = costFunction;
        compileCostFunction();
    }

    private void compileCostFunction() {
        this.compiledCostFunction = new ExpressionBuilder(costFunction)
                .variable("x")
                .build();
    }

    public void setCurrentFlow(double flow) {
        this.currentFlow = flow;
        this.compiledCostFunction.setVariable("x", flow);
    }

    public double getCurrentCost() {
        return compiledCostFunction.evaluate();
    }

    // Getters and setters for currentFlow and costFunction...
}

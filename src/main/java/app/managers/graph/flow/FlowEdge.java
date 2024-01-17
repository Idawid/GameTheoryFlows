package app.managers.graph.flow;

import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;

public class FlowEdge extends Edge {
    private double currentFlow;
    private String costFunction; // Mathematical expression as a string
    private transient Expression compiledCostFunction;

    public FlowEdge(Vertex from, Vertex to, String costFunction) {
        super(from, to);
        setCostFunction(costFunction);
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
        compiledCostFunction.setVariable("x", currentFlow);
        return compiledCostFunction.evaluate();
    }

    public double getCurrentFlow() {
        return currentFlow;
    }

    public String getCostFunction() {
        return costFunction;
    }

    public void setCostFunction(String costFunction) {
        Expression dummyExp = new ExpressionBuilder(costFunction).variable("x").build().setVariable("x", 0.0);
        ValidationResult res = dummyExp.validate();
        if (!res.isValid()) {
            // Expression costFunction is not valid
            throw new IllegalArgumentException("Invalid cost function");
        }

        this.costFunction = costFunction;
        this.compileCostFunction();
    }
}

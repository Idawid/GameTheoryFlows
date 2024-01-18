package app.managers.graph.flow;

import app.Observable;
import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
import app.Observer;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public class FlowEdge extends Edge implements Observable {
    private double currentFlow;
    private String costFunction; // Mathematical expression as a string
    private transient Expression compiledCostFunction;
    private List<Observer> observers = new ArrayList<>();

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
        notifyObservers();
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
        notifyObservers();
    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this);
        }
    }
}

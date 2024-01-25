package app.managers.graph.flow;

import app.Observable;
import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
import app.Observer;
import javafx.application.Platform;
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
        try {
            Platform.runLater(() -> notifyObservers());
        } catch (IllegalStateException e) {
            // JavaFx not started - ok
        }
    }

    public double getCurrentCost() {
        compiledCostFunction.setVariable("x", currentFlow);
        return compiledCostFunction.evaluate();
    }

    public double getPotentialCost() {
        double integral = 0;
        int steps = 20; // Number of steps for the approximation
        double stepSize = currentFlow / steps;

        for (int i = 0; i < steps; i++) {
            double x1 = i * stepSize;
            double x2 = (i + 1) * stepSize;

            this.compiledCostFunction.setVariable("x", x1);
            double y1 = compiledCostFunction.evaluate();

            this.compiledCostFunction.setVariable("x", x2);
            double y2 = compiledCostFunction.evaluate();

            integral += (y1 + y2) * stepSize / 2; // Area of the trapezoid
        }

        return integral;
    }

    public double getMarginalCost() {
        // d/dx(x*c(x))

        double deltaX = 1e-5; // Small change in x
        double xPlusDeltaX = currentFlow + deltaX;

        // Calculate x * c(x)
        this.compiledCostFunction.setVariable("x", currentFlow);
        double fx = currentFlow * compiledCostFunction.evaluate();

        // Calculate (x + Δx) * c(x + Δx)
        this.compiledCostFunction.setVariable("x", xPlusDeltaX);
        double fxPlusDeltaX = xPlusDeltaX * compiledCostFunction.evaluate();

        // Approximate the derivative
        double marginalCost = (fxPlusDeltaX - fx) / deltaX;

        return marginalCost;
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
        try {
            Platform.runLater(() -> notifyObservers());
        } catch (IllegalStateException e) {
            // JavaFx not started - ok
        }
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

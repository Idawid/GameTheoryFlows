package app.managers.graph.flow;

import app.Observable;
import app.Observer;
import app.managers.graph.common.Vertex;

import java.util.ArrayList;
import java.util.List;

public class FlowVertex extends Vertex implements Observable {
    private boolean isSource;
    private boolean isSink;
    private double flowCapacity;
    private List<Observer> observers = new ArrayList<>();

    public FlowVertex(double x, double y) {
        super(x, y);
        this.isSource = false;
        this.isSink = false;
        this.flowCapacity = 0.0;
    }

    public boolean isSource() {
        return isSource;
    }

    public void setSource(boolean isSource) {
        // Ensure that a vertex cannot be both a source and a sink
        if (isSource && isSink) {
            isSink = false;
        }
        this.isSource = isSource;
        notifyObservers();
    }

    public boolean isSink() {
        return isSink;
    }

    public void setSink(boolean isSink) {
        // Ensure that a vertex cannot be both a source and a sink
        if (isSink && isSource) {
            isSource = false;
            flowCapacity = 0.0;
        }
        this.isSink = isSink;
        notifyObservers();
    }

    public double getFlowCapacity() {
        return flowCapacity;
    }

    public void setFlowCapacity(double flowCapacity) {
        // Flow capacity is only valid for source vertices
        if (!isSource) {
            throw new IllegalStateException("Flow capacity is only valid for source vertices.");
        }
        if (flowCapacity < 0.0) {
            throw new IllegalArgumentException("Flow capacity cannot be negative.");
        }
        this.flowCapacity = flowCapacity;
        notifyObservers();
    }

    public String toString() {
        if (isSink()) {
            return "T";
        }
        else if (isSource()) {
            return "S";
        }
        else return String.valueOf(getId());
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



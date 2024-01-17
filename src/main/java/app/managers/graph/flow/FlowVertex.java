package app.managers.graph.flow;

import app.managers.graph.common.Vertex;

public class FlowVertex extends Vertex {
    private boolean isSource;
    private boolean isSink;
    private double flowCapacity;

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
            throw new IllegalArgumentException("A vertex cannot be both a source and a sink.");
        }
        this.isSource = isSource;
    }

    public boolean isSink() {
        return isSink;
    }

    public void setSink(boolean isSink) {
        // Ensure that a vertex cannot be both a source and a sink
        if (isSink && isSource) {
            throw new IllegalArgumentException("A vertex cannot be both a source and a sink.");
        }
        this.isSink = isSink;
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
    }
}



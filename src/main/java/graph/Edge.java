package graph;

public interface Edge extends Identifiable {
    Vertex getStartVertex();
    Vertex getEndVertex();
    double getCost();
}

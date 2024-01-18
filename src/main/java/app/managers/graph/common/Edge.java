package app.managers.graph.common;

public class Edge {
    private Vertex from;
    private Vertex to;

    public Edge(Vertex from, Vertex to) {
        this.from = from;
        this.to = to;
    }

    public Vertex getFrom() {
        return from;
    }

    public void setFrom(Vertex from) {
        this.from = from;
    }

    public Vertex getTo() {
        return to;
    }

    public void setTo(Vertex to) {
        this.to = to;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Edge otherEdge = (Edge) obj;

        // Check if the edge is equivalent regardless of vertex order
        return (from.equals(otherEdge.from) && to.equals(otherEdge.to)) ||
                (from.equals(otherEdge.to) && to.equals(otherEdge.from));
    }
}


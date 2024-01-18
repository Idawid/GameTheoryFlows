package app.managers.graph.common;

public class Vertex {
    private static int idCounter = 0;
    private int id;
    private double x;
    private double y;

    public Vertex(double x, double y) {
        this.id = idCounter++;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}


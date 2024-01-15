package visualization;

import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.List;

public class GraphManagerFX {
    private Pane canvas;
    private List<VertexFX> vertices;
    private List<EdgeFX> edges;

    public GraphManagerFX(Pane canvas) {
        this.canvas = canvas;
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public void addVertex(double x, double y) {
        // Create and add a vertex
    }

    public void addEdge(VertexFX start, VertexFX end) {
        // Create and add an edge
    }

    // Methods for removing vertices and edges, and other graph logic.
}

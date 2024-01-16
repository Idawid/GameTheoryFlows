package app;

import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
import app.managers.view.common.SelectionType;

public class AppStateMemento {
    private final SelectionType selectionType;
    private final Vertex selectedVertex;
    private final Edge selectedEdge;
    private final boolean isDrawingEdge;

    public AppStateMemento(AppState state) {
        this.selectionType = state.getSelectionType();
        this.selectedVertex = state.getSelectedVertex();
        this.selectedEdge = state.getSelectedEdge();
        this.isDrawingEdge = state.isDrawingEdge();
    }

    public SelectionType getSelectionType() {
        return selectionType;
    }

    public Vertex getSelectedVertex() {
        return selectedVertex;
    }

    public Edge getSelectedEdge() {
        return selectedEdge;
    }

    public boolean isDrawingEdge() {
        return isDrawingEdge;
    }
}

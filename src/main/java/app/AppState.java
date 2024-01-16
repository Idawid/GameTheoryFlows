package app;

import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
import app.managers.view.common.SelectionType;

public class AppState {
    private SelectionType selectionType;
    private Vertex selectedVertex;
    private Edge selectedEdge;
    private boolean isDrawingEdge;

    public AppState() {
        this.selectionType = SelectionType.NONE;
        this.selectedVertex = null;
        this.selectedEdge = null;
        this.isDrawingEdge = false;
    }

    public SelectionType getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(SelectionType selectionType) {
        this.selectionType = selectionType;
    }

    public Vertex getSelectedVertex() {
        return selectedVertex;
    }

    public void setSelectedVertex(Vertex selectedVertex) {
        this.selectedVertex = selectedVertex;
        this.selectionType = SelectionType.VERTEX; // Automatically update selection type
    }

    public Edge getSelectedEdge() {
        return selectedEdge;
    }

    public void setSelectedEdge(Edge selectedEdge) {
        this.selectedEdge = selectedEdge;
        this.selectionType = SelectionType.EDGE; // Automatically update selection type
    }

    public boolean isDrawingEdge() {
        return isDrawingEdge;
    }

    public void setDrawingEdge(boolean isDrawingEdge) {
        this.isDrawingEdge = isDrawingEdge;
    }

    public void restoreFromMemento(AppStateMemento previousState) {
        this.selectionType = previousState.getSelectionType();
        this.selectedVertex = previousState.getSelectedVertex();
        this.selectedEdge = previousState.getSelectedEdge();
        this.isDrawingEdge = previousState.isDrawingEdge();
    }
}


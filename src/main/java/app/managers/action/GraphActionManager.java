package app.managers.action;

import app.*;
import app.managers.action.common.*;
import app.managers.graph.GraphManager;
import app.managers.graph.common.Edge;
import app.managers.graph.common.Vertex;
import app.managers.graph.flow.FlowEdge;
import app.managers.graph.flow.FlowVertex;
import app.managers.view.GraphViewManager;

import java.util.Stack;

public class GraphActionManager {
    private Stack<Action> undoStack;
    private Stack<Action> redoStack;
    private Stack<AppStateMemento> undoStateStack;
    private Stack<AppStateMemento> redoStateStack;
    private GraphManager graphManager;
    private GraphViewManager viewManager;
    private AppState state;

    public GraphActionManager(GraphManager graphManager, GraphViewManager viewManager, AppState initialState) {
        this.state = initialState;
        this.graphManager = graphManager;
        this.viewManager = viewManager;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.undoStateStack = new Stack<>();
        this.redoStateStack = new Stack<>();
    }

    private void performAction(Action action) {
        AppStateMemento stateBeforeAction = new AppStateMemento(state);
        undoStateStack.push(stateBeforeAction);

        action.perform();
        // Clear redo stacks
        redoStack.clear();
        redoStateStack.clear();

        undoStack.push(action);
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Action lastAction = undoStack.pop();
            AppStateMemento previousState = undoStateStack.pop();

            lastAction.undo();

            redoStateStack.push(new AppStateMemento(state));
            redoStack.push(lastAction);

            state.restoreFromMemento(previousState);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Action nextAction = redoStack.pop();
            AppStateMemento nextState = redoStateStack.pop();

            nextAction.perform();

            undoStateStack.push(new AppStateMemento(state));
            undoStack.push(nextAction);

            state.restoreFromMemento(nextState);
        }
    }

    public void addVertex(Vertex vertex) {
        AddVertexAction addVertexAction = new AddVertexAction(vertex, graphManager, viewManager);
        performAction(addVertexAction);
    }

    public void removeVertex(Vertex vertex) {
        RemoveVertexAction removeVertexAction = new RemoveVertexAction(vertex, graphManager, viewManager);
        performAction(removeVertexAction);
    }

    public void selectVertex(Vertex vertex) {
        SelectVertexAction selectVertexAction = new SelectVertexAction(vertex, graphManager, viewManager);
        performAction(selectVertexAction);
    }

    public void addEdge(Edge edge) {
        AddEdgeAction addEdgeAction = new AddEdgeAction(edge, graphManager, viewManager);
        performAction(addEdgeAction);
    }

    public void removeEdge(Edge edge) {
        RemoveEdgeAction removeEdgeAction = new RemoveEdgeAction(edge, graphManager, viewManager);
        performAction(removeEdgeAction);
    }

    public void selectEdge(Edge edge) {
        SelectEdgeAction selectEdgeAction = new SelectEdgeAction(edge, graphManager, viewManager);
        performAction(selectEdgeAction);
    }

    public void clearGraph() {
        ClearGraphAction clearGraphAction = new ClearGraphAction(graphManager, viewManager, state.getSelectedVertex(), state.getSelectedEdge());
        performAction(clearGraphAction);
    }

    public void addEdge(Vertex vertex, Vertex vertex1) {
        addEdge(new FlowEdge(vertex, vertex1, "1"));
    }
}



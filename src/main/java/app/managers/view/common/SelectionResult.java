package app.managers.view.common;

public class SelectionResult {
    private final SelectionType type;
    private final Object selectedObject;

    public SelectionResult(SelectionType type, Object selectedObject) {
        this.type = type;
        this.selectedObject = selectedObject;
    }

    public SelectionType getType() {
        return type;
    }

    public Object getSelectedObject() {
        return selectedObject;
    }
}

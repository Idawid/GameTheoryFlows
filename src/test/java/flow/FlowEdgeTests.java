package flow;

import static org.junit.Assert.assertEquals;

import app.managers.graph.common.Vertex;
import app.managers.graph.flow.FlowEdge;
import org.junit.Before;
import org.junit.Test;

public class FlowEdgeTests {

    private FlowEdge flowEdge;

    @Before
    public void setUp() {
        // Create a FlowEdge with a cost function and set the current flow to 2
        flowEdge = new FlowEdge(new Vertex(0, 0), new Vertex(1, 1), "2 * x + 3");
        flowEdge.setCurrentFlow(2.0);
    }

    @Test
    public void testSetCurrentFlow() {
        // flow = 2.0 -> 5.0
        // costFunction = 2 * x + 3
        flowEdge.setCurrentFlow(5.0);
        assertEquals(13.0, flowEdge.getCurrentCost(), 1e-6);
    }

    @Test
    public void testSetCostFunction() {
        // flow = 2.0
        // costFunction = 2 * x + 3 -> x * x + 1
        flowEdge.setCostFunction("x * x + 1");
        assertEquals(5.0, flowEdge.getCurrentCost(), 1e-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCostFunctionWithInvalidExpression() {
        // Attempt to set an invalid cost function, expecting an IllegalArgumentException
        flowEdge.setCostFunction("2 *"); // Invalid expression
    }
}
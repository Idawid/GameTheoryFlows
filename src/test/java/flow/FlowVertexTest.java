package flow;

import app.managers.graph.flow.FlowVertex;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FlowVertexTest {
    private FlowVertex sourceVertex;
    private FlowVertex sinkVertex;
    private FlowVertex normalVertex;

    @Before
    public void setUp() {
        sourceVertex = new FlowVertex(0, 0);
        sourceVertex.setSource(true);
        sourceVertex.setFlowCapacity(10.0);

        sinkVertex = new FlowVertex(1, 1);
        sinkVertex.setSink(true);

        normalVertex = new FlowVertex(2, 2);
    }

    @Test
    public void testSourceVertexIsSource() {
        assertTrue(sourceVertex.isSource());
    }

    @Test
    public void testSinkVertexIsSink() {
        assertTrue(sinkVertex.isSink());
    }

    @Test
    public void testNormalVertexIsNeitherSourceNorSink() {
        assertFalse(normalVertex.isSource());
        assertFalse(normalVertex.isSink());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVertexCannotBeBothSourceAndSink() {
        normalVertex.setSource(true);
        normalVertex.setSink(true);
    }

    @Test(expected = IllegalStateException.class)
    public void testFlowCapacityInvalidForNonSourceVertex() {
        normalVertex.setFlowCapacity(5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFlowCapacityCannotBeNegative() {
        sourceVertex.setFlowCapacity(-5.0);
    }
}


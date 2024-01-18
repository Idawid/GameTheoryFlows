package flow;

import app.managers.graph.common.Vertex;
import app.managers.graph.flow.FlowEdge;
import app.managers.graph.flow.FlowGraph;
import app.managers.graph.flow.FlowPath;
import app.managers.graph.flow.FlowVertex;

import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;
import org.junit.Test;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;


import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FlowGraphTest {
    @Test
    public void testFindAllPathsBasic() {
        FlowGraph graph = new FlowGraph();
        FlowVertex v1 = new FlowVertex(0, 0);
        FlowVertex v2 = new FlowVertex(0, 0);
        v1.setSource(true);
        v2.setSink(true);
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addEdge(new FlowEdge(v1, v2, "1"));

        List<FlowPath> paths = graph.findAllPaths();
        int expectedNumberOfPaths = 1; // Adjust based on your graph setup
        assertEquals(expectedNumberOfPaths, paths.size());
    }

    @Test
    public void testFindAllPathsComplex() {
        FlowGraph graph = new FlowGraph();

        // Create 10 vertices, set some as sources and sinks
        for (int i = 0; i < 10; i++) {
            FlowVertex vertex = new FlowVertex(i, i); // x, y can be arbitrary
            graph.addVertex(vertex);
            if (i == 0 || i == 1) vertex.setSource(true); // First two as sources
            if (i == 8 || i == 9) vertex.setSink(true);   // Last two as sinks
        }

        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(2), "1"));
        graph.addEdge(new FlowEdge(graph.getVertex(2), graph.getVertex(4), "1"));
        graph.addEdge(new FlowEdge(graph.getVertex(4), graph.getVertex(9), "1"));
        graph.addEdge(new FlowEdge(graph.getVertex(1), graph.getVertex(3), "1"));
        graph.addEdge(new FlowEdge(graph.getVertex(3), graph.getVertex(5), "1"));
        graph.addEdge(new FlowEdge(graph.getVertex(5), graph.getVertex(9), "1"));
        graph.addEdge(new FlowEdge(graph.getVertex(4), graph.getVertex(7), "1"));
        graph.addEdge(new FlowEdge(graph.getVertex(5), graph.getVertex(7), "1"));
        graph.addEdge(new FlowEdge(graph.getVertex(7), graph.getVertex(8), "1"));

        List<FlowPath> paths = graph.findAllPaths();

        // Expected number of paths will depend on how you set up the graph
        int expectedNumberOfPaths = 4; // Define based on your graph structure
        assertEquals(expectedNumberOfPaths, paths.size());
    }

    @Test
    public void TestFindOptimalFlow() {
        FlowGraph graph = new FlowGraph();

        // Create 4 vertices, set some as sources and sinks
        for (int i = 0; i < 4; i++) {
            FlowVertex vertex = new FlowVertex(i, i); // x, y can be arbitrary
            graph.addVertex(vertex);
            if (i == 0) vertex.setSource(true); // First one as source
            if (i == 3) vertex.setSink(true);   // Last one as sink
        }

        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(1), "x"));
        graph.addEdge(new FlowEdge(graph.getVertex(1), graph.getVertex(3), "1"));
        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(2), "1"));
        graph.addEdge(new FlowEdge(graph.getVertex(2), graph.getVertex(3), "x"));
        graph.addEdge(new FlowEdge(graph.getVertex(1), graph.getVertex(2), "x"));

        List<FlowPath> paths = graph.findAllPaths();

        // Expected number of paths will depend on how you set up the graph
        int expectedNumberOfPaths = 3; // Define based on your graph structure
        assertEquals(expectedNumberOfPaths, paths.size());

        double T = 1.0;

        MultivariateFunction function = point -> {
            double totalCost = 0.0;
            double remainingFlow = T;
            double penalty = 1000000;

            double[] flows = new double[point.length + 1];
            for (int i = 0; i < point.length; i++) {
                flows[i] = point[i];
                remainingFlow -= point[i];
            }
            flows[point.length] = remainingFlow;

            if (Arrays.stream(flows).sum() > T) {
                totalCost += penalty;
            }

            for (int i = 0; i < flows.length; i++) {
                for (FlowEdge edge : paths.get(i).getEdges()) {
                    edge.setCurrentFlow(edge.getCurrentFlow() + flows[i]);
                }
            }

            for (int i = 0; i < flows.length; i++) {
                for (FlowEdge edge : paths.get(i).getEdges()) {
                    totalCost += flows[i] * edge.getCurrentCost();
                }
            }

            for (FlowPath path : paths) {
                for (FlowEdge edge : path.getEdges()) {
                    edge.setCurrentFlow(0.0);
                }
            }

            return totalCost;
        };

        double[] lowerBound = new double[paths.size() - 1]; // Lower bounds (all zeros)
        double[] upperBound = new double[paths.size() - 1]; // Upper bounds (optional, can be set to a large number or left unbounded)
        Arrays.fill(lowerBound, 0.0);
        Arrays.fill(upperBound, Double.POSITIVE_INFINITY); // Or any large value that makes sense in your context

        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(2 * (paths.size() - 1) + 1); // Number of interpolation points
        PointValuePair optimum = optimizer.optimize(
                new MaxIter(100),
                new MaxEval(100),
                new ObjectiveFunction(function),
                GoalType.MINIMIZE,
                new InitialGuess(new double[paths.size() - 1]), // Initial guess for all flows except the last
                new SimpleBounds(lowerBound, upperBound) // Enforce the bounds
        );

        double optimumCost = function.value(optimum.getPoint());
        optimumCost = Math.round(optimumCost * 100.0) / 100.0;
        System.out.println("Optimal flow avg. cost: " + optimumCost);

        double[] optimumFlows = Arrays.copyOf(optimum.getPoint(), optimum.getPoint().length + 1);
        optimumFlows[optimumFlows.length - 1] = T - Arrays.stream(optimum.getPoint()).sum();
        optimumFlows = Arrays.stream(optimumFlows).map(d -> Math.round(d * 100.0) / 100.0).toArray();

        for (int i = 0; i < optimumFlows.length; i++) {
            System.out.println("Optimal flow for path " + (i) + " ["+ paths.get(i).getCostFunction() + "] : " + optimumFlows[i] );
        }
    }
}

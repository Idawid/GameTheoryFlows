//package flow;
//
//import app.managers.graph.flow.FlowEdge;
//import app.managers.graph.flow.FlowGraph;
//import app.managers.graph.flow.FlowPath;
//import app.managers.graph.flow.FlowVertex;
//
//import org.apache.commons.math3.optim.*;
//import org.apache.commons.math3.optim.linear.LinearConstraint;
//import org.apache.commons.math3.optim.linear.Relationship;
//import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
//import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
//import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
//import org.junit.Test;
//import org.apache.commons.math3.analysis.MultivariateFunction;
//import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
//import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
//
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.Assert.*;
//
//public class FlowGraphTest {
//    @Test
//    public void testFindAllPathsBasic() {
//        FlowGraph graph = new FlowGraph();
//        FlowVertex v1 = new FlowVertex(0, 0);
//        FlowVertex v2 = new FlowVertex(0, 0);
//        v1.setSource(true);
//        v2.setSink(true);
//        graph.addVertex(v1);
//        graph.addVertex(v2);
//        graph.addEdge(new FlowEdge(v1, v2, "1"));
//
//        List<FlowPath> paths = graph.findAllPaths();
//        int expectedNumberOfPaths = 1; // Adjust based on your graph setup
//        assertEquals(expectedNumberOfPaths, paths.size());
//    }
//
//    @Test
//    public void testFindAllPathsComplex() {
//        FlowGraph graph = new FlowGraph();
//
//        // Create 10 vertices, set some as sources and sinks
//        for (int i = 0; i < 10; i++) {
//            FlowVertex vertex = new FlowVertex(i, i); // x, y can be arbitrary
//            graph.addVertex(vertex);
//            if (i == 0 || i == 1) {
//                vertex.setSource(true); // First two as sources
//                vertex.setFlowCapacity(10.0);
//            }
//            if (i == 8 || i == 9) vertex.setSink(true);   // Last two as sinks
//        }
//
//        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(2), "1"));
//        graph.addEdge(new FlowEdge(graph.getVertex(2), graph.getVertex(4), "1"));
//        graph.addEdge(new FlowEdge(graph.getVertex(4), graph.getVertex(9), "1"));
//        graph.addEdge(new FlowEdge(graph.getVertex(1), graph.getVertex(3), "1"));
//        graph.addEdge(new FlowEdge(graph.getVertex(3), graph.getVertex(5), "1"));
//        graph.addEdge(new FlowEdge(graph.getVertex(5), graph.getVertex(9), "1"));
//        graph.addEdge(new FlowEdge(graph.getVertex(4), graph.getVertex(7), "1"));
//        graph.addEdge(new FlowEdge(graph.getVertex(5), graph.getVertex(7), "1"));
//        graph.addEdge(new FlowEdge(graph.getVertex(7), graph.getVertex(8), "1"));
//
//        List<FlowPath> paths = graph.findAllPaths();
//
//        // Expected number of paths will depend on how you set up the graph
//        int expectedNumberOfPaths = 8; // Define based on your graph structure
//        assertEquals(expectedNumberOfPaths, paths.size());
//    }
//
//    @Test
//    public void TestFindOptimalFlow() {
//        FlowGraph graph = createExercise2Graph();
//
//        List<FlowPath> paths = graph.findAllPaths();
//
//        double T = graph.calculateAvailableFlow();
//
//
//        MultivariateFunction function = point -> {
//            double totalCost = 0.0;
//            double penalty = 100;
//
//            double[] flows = calculateFlowsFromPoint(T, point);
//
//            // Apply penalty if necessary
//            if (Arrays.stream(flows).anyMatch(flow -> flow < -10e-4) || Arrays.stream(flows).sum() > T) {
//                totalCost += penalty;
//            }
//
//            // Prepare flows in the graph
//            fillGraphFlows(paths, flows);
//
//            // Cost calculation
//            for (int i = 0; i < flows.length; i++) {
//                for (FlowEdge edge : paths.get(i).getEdges()) {
//                    totalCost += flows[i] * edge.getCurrentCost();
//                }
//            }
//
//            // Reset flows in the graph
//            resetGraphFlows(paths);
//
//            return totalCost;
//        };
//
//        double[] lowerBound = new double[paths.size() - 1]; // Lower bounds (all zeros)
//        double[] upperBound = new double[paths.size() - 1]; // Upper bounds (optional, can be set to a large number or left unbounded)
//        Arrays.fill(lowerBound, 0.0);
//        Arrays.fill(upperBound, Double.POSITIVE_INFINITY); // Or any large value that makes sense in your context
//
//        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(2 * (paths.size() - 1) + 1); // Number of interpolation points
//        PointValuePair optimum = optimizer.optimize(
//                new MaxIter(1000),
//                new MaxEval(1000),
//                new ObjectiveFunction(function),
//                GoalType.MINIMIZE,
//                new InitialGuess(new double[paths.size() - 1]), // Initial guess for all flows except the last
//                new SimpleBounds(lowerBound, upperBound) // Enforce the bounds
//        );
//
//        double[] resultantFlows = calculateFlowsFromPoint(T, optimum.getPoint());
//        double cost = calculateTotalCost(paths, resultantFlows);
//
//        System.out.println("Optimal flow avg. cost: " + cost);
//
//        fillGraphFlows(paths, resultantFlows);
//        resultantFlows = Arrays.stream(resultantFlows).map(d -> Math.round(d * 100.0) / 100.0).toArray();
//        for (int i = 0; i < resultantFlows.length; i++) {
//            if (resultantFlows[i] > 0)
//                System.out.println("Optimal flow for path " + (i) + " [" + paths.get(i).getCostFunction() + "] : " + "x=" + resultantFlows[i] + " [C(x)= " + paths.get(i).getCurrentCost() + "]");
//        }
//    }
//
//    @Test
//    public void TestFindOptimalFlowDiscrete() {
//        FlowGraph graph = createDiscreteBraessGraph();
//
//        List<FlowPath> paths = graph.findAllPaths();
//
//        double T = graph.calculateAvailableFlow();
//
//
//        MultivariateFunction function = point -> {
//            double totalCost = 0.0;
//            double penalty = 1000;
//
//            double[] flows = calculateIntegerFlowsFromPoint(T, point);
//
//            // Apply penalty if necessary
//            if (Arrays.stream(flows).anyMatch(flow -> flow < -10e-4) || Arrays.stream(flows).sum() > T) {
//                totalCost += penalty;
//            }
//
//            // Prepare flows in the graph
//            fillGraphFlows(paths, flows);
//
//            // Cost calculation
//            for (int i = 0; i < flows.length; i++) {
//                for (FlowEdge edge : paths.get(i).getEdges()) {
//                    totalCost += flows[i] * edge.getCurrentCost();
//                }
//            }
//
//            // Reset flows in the graph
//            resetGraphFlows(paths);
//
//            return totalCost;
//        };
//
//        double[] lowerBound = new double[paths.size() - 1]; // Lower bounds (all zeros)
//        double[] upperBound = new double[paths.size() - 1]; // Upper bounds (optional, can be set to a large number or left unbounded)
//        Arrays.fill(lowerBound, 0.0);
//        Arrays.fill(upperBound, Double.POSITIVE_INFINITY);
//
//        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(2 * (paths.size() - 1) + 1); // Number of interpolation points
//        PointValuePair optimum = optimizer.optimize(
//                new MaxIter(10000),
//                new MaxEval(10000),
//                new ObjectiveFunction(function),
//                GoalType.MINIMIZE,
//                new InitialGuess(new double[paths.size() - 1]), // Initial guess for all flows except the last
//                new SimpleBounds(lowerBound, upperBound) // Enforce the bounds
//        );
//
//        double[] resultantFlows = calculateIntegerFlowsFromPoint(T, optimum.getPoint());
//        double cost = calculateTotalCost(paths, resultantFlows);
//
//        System.out.println("Optimal flow avg. cost: " + cost);
//
//        fillGraphFlows(paths, resultantFlows);
//        resultantFlows = Arrays.stream(resultantFlows).map(d -> Math.round(d * 100.0) / 100.0).toArray();
//        for (int i = 0; i < resultantFlows.length; i++) {
//            if (resultantFlows[i] > 0)
//                System.out.println("Optimal flow for path " + (i) + " [" + paths.get(i).getCostFunction() + "] : " + "x=" + resultantFlows[i] + " [C(x)= " + paths.get(i).getCurrentCost() + "]");
//        }
//    }
//
//    @Test
//    public void TestFindNashFlow() {
//        FlowGraph graph = createExercise2Graph();
//
//        List<FlowPath> paths = graph.findAllPaths();
//
//        double T = graph.calculateAvailableFlow();
//
//
//        MultivariateFunction function = point -> {
//            double totalCost = 0;
//            double penalty = 1000;
//
//            double[] flows = calculateFlowsFromPoint(T, point);
//
//            if (Arrays.stream(flows).anyMatch(flow -> flow < -10e-4) || Arrays.stream(flows).sum() > T) {
//                totalCost += penalty; // Penalize invalid distributions
//            }
//
//            fillGraphFlows(paths, flows);
//
//            double[] costs = new double[flows.length];
//
//            for (int i = 0; i < flows.length; i++) {
//                for (FlowEdge edge : paths.get(i).getEdges()) {
//                    costs[i] += edge.getCurrentCost();
//                }
//            }
//
//            resetGraphFlows(paths);
//
//            totalCost += calculateNashObjectiveValue(costs, flows);
//
//            // Objective: Minimize the variance or dissimilarity of costs
//            return totalCost;
//        };
//
//        SimplexOptimizer optimizer = new SimplexOptimizer(1e-5, 1e-5); // Number of interpolation points
//        // create a simplex
//        double[] simplexSizes = new double[paths.size() - 1];
//        Arrays.fill(simplexSizes, 1000.0);
//        NelderMeadSimplex simplex = new NelderMeadSimplex(simplexSizes);
//
//        double[] initialGuess = new double[paths.size() - 1];
//        Arrays.fill(initialGuess, T / (paths.size() - 1));
//
//        PointValuePair optimum = optimizer.optimize(
//                new MaxIter(100000),
//                new MaxEval(100000),
//                new ObjectiveFunction(function),
//                GoalType.MINIMIZE,
//                new InitialGuess(initialGuess), // Initial guess for all flows except the last
//                simplex
//        );
//
//        double[] resultantFlows = calculateFlowsFromPoint(T, optimum.getPoint());
//        double cost = calculateTotalCost(paths, resultantFlows);
//
//        System.out.println("Nash flow avg. cost: " + cost);
//
//        fillGraphFlows(paths, resultantFlows);
//        resultantFlows = Arrays.stream(resultantFlows).map(d -> Math.round(d * 100.0) / 100.0).toArray();
//        for (int i = 0; i < resultantFlows.length; i++) {
//            if (resultantFlows[i] > 0)
//                System.out.println("Nash flow for path " + (i) + " [" + paths.get(i).getCostFunction() + "] : " + "x=" + resultantFlows[i] + " [C(x)= " + paths.get(i).getCurrentCost() + "]");
//        }
//    }
//
//    @Test
//    public void TestFindNashFlowDiscrete() {
//        FlowGraph graph = createDiscreteBraessGraph();
//
//        List<FlowPath> paths = graph.findAllPaths();
//
//        double T = graph.calculateAvailableFlow();
//
//        MultivariateFunction function = objectiveFunction(T, paths, true, false);
//
//        double[] lowerBound = new double[paths.size() - 1]; // Lower bounds (all zeros)
//        double[] upperBound = new double[paths.size() - 1]; // Upper bounds (optional, can be set to a large number or left unbounded)
//        Arrays.fill(lowerBound, 0.0);
//        Arrays.fill(upperBound, Double.POSITIVE_INFINITY); // Or any large value that makes sense in your context
//
//        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(2 * (paths.size() - 1) + 1); // Number of interpolation points
//        PointValuePair optimum = optimizer.optimize(
//                new MaxIter(1000),
//                new MaxEval(1000),
//                new ObjectiveFunction(function),
//                GoalType.MINIMIZE,
//                new InitialGuess(new double[paths.size() - 1]), // Initial guess for all flows except the last
//                new SimpleBounds(lowerBound, upperBound) // Enforce the bounds
//        );
//
//        double[] resultantFlows = calculateIntegerFlowsFromPoint(T, optimum.getPoint());
//        double cost = calculateTotalCost(paths, resultantFlows);
//
//        System.out.println("Nash flow avg. cost: " + cost);
//
//        fillGraphFlows(paths, resultantFlows);
//        resultantFlows = Arrays.stream(resultantFlows).map(d -> Math.round(d * 100.0) / 100.0).toArray();
//        for (int i = 0; i < resultantFlows.length; i++) {
//            if (resultantFlows[i] > 0)
//                System.out.println("Nash flow for path " + (i) + " [" + paths.get(i).getCostFunction() + "] : " + "x=" + resultantFlows[i] + " [C(x)= " + paths.get(i).getCurrentCost() + "]");
//        }
//    }
//
//    private FlowGraph createBraessGraph() {
//        FlowGraph graph = new FlowGraph();
//
//        // Create 4 vertices, set some as sources and sinks
//        for (int i = 0; i < 4; i++) {
//            FlowVertex vertex = new FlowVertex(i, i); // x, y can be arbitrary
//            graph.addVertex(vertex);
//            if (i == 0) {
//                vertex.setSource(true); // First one as source
//                vertex.setFlowCapacity(1.0);
//            }
//            if (i == 3) vertex.setSink(true);   // Last one as sink
//        }
//
//        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(1), "x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(1), graph.getVertex(3), "1"));
//        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(2), "1"));
//        graph.addEdge(new FlowEdge(graph.getVertex(2), graph.getVertex(3), "x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(1), graph.getVertex(2), "x"));
//
//        return graph;
//    }
//
//    private FlowGraph createDiscreteBraessGraph() {
//        FlowGraph graph = new FlowGraph();
//
//        // Create 4 vertices, set some as sources and sinks
//        for (int i = 0; i < 4; i++) {
//            FlowVertex vertex = new FlowVertex(i, i); // x, y can be arbitrary
//            graph.addVertex(vertex);
//            if (i == 0) {
//                vertex.setSource(true); // First one as source
//                vertex.setFlowCapacity(10.0);
//            }
//            if (i == 3) vertex.setSink(true);   // Last one as sink
//        }
//
//        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(1), "x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(1), graph.getVertex(3), "10"));
//        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(2), "10"));
//        graph.addEdge(new FlowEdge(graph.getVertex(2), graph.getVertex(3), "x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(1), graph.getVertex(2), "x"));
//
//        return graph;
//    }
//
//    private FlowGraph createExercise2Graph() {
//        FlowGraph graph = new FlowGraph();
//
//        // Create 6 vertices, set some as sources and sinks
//        for (int i = 0; i < 6; i++) {
//            FlowVertex vertex = new FlowVertex(i, i); // x, y can be arbitrary
//            graph.addVertex(vertex);
//            if (i == 0) {
//                vertex.setSource(true); // First one as source
//                vertex.setFlowCapacity(1.0);
//            }
//            if (i == 4) vertex.setSink(true);   // Last one as sink
//        }
//
//        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(1), "1"));
//        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(2), "x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(3), "x"));
//
//        graph.addEdge(new FlowEdge(graph.getVertex(2), graph.getVertex(3), "1"));
//        graph.addEdge(new FlowEdge(graph.getVertex(1), graph.getVertex(3), "x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(1), graph.getVertex(4), "2*x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(3), graph.getVertex(4), "2*x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(3), graph.getVertex(5), "x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(5), graph.getVertex(4), "1"));
//
//        return graph;
//    }
//
//    private FlowGraph createDiscreteExercise2Graph() {
//        FlowGraph graph = new FlowGraph();
//
//        // Create 6 vertices, set some as sources and sinks
//        for (int i = 0; i < 6; i++) {
//            FlowVertex vertex = new FlowVertex(i, i); // x, y can be arbitrary
//            graph.addVertex(vertex);
//            if (i == 0) {
//                vertex.setSource(true); // First one as source
//                vertex.setFlowCapacity(10.0);
//            }
//            if (i == 4) vertex.setSink(true);   // Last one as sink
//        }
//
//        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(1), "10"));
//        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(2), "x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(0), graph.getVertex(3), "x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(2), graph.getVertex(3), "10"));
//        graph.addEdge(new FlowEdge(graph.getVertex(3), graph.getVertex(1), "x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(1), graph.getVertex(3), "x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(1), graph.getVertex(4), "2*x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(3), graph.getVertex(4), "2*x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(3), graph.getVertex(5), "x"));
//        graph.addEdge(new FlowEdge(graph.getVertex(4), graph.getVertex(5), "10"));
//
//        return graph;
//    }
//
//
//    private MultivariateFunction objectiveFunction(double T, List<FlowPath> paths, boolean isDiscrete, boolean isGoalOptimum) {
//
//        MultivariateFunction function = point -> {
//            // Calculate the flows from the point
//            double[] flows;
//            if (isDiscrete) {
//                flows = calculateIntegerFlowsFromPoint(T, point);
//            }
//            else {
//                flows = calculateFlowsFromPoint(T, point);
//            }
//
//            // Calculate the current costs given the flows in the graph
//            fillGraphFlows(paths, flows);
//
//            double[] costs = new double[flows.length];
//            if (isGoalOptimum) {
//                for (int i = 0; i < flows.length; i++) {
//                    for (FlowEdge edge : paths.get(i).getEdges()) {
//                        costs[i] += edge.getMarginalCost();
//                    }
//                }
//            }
//            else {
//                for (int i = 0; i < flows.length; i++) {
//                    for (FlowEdge edge : paths.get(i).getEdges()) {
//                        costs[i] += edge.getPotentialCost();
//                    }
//                }
//            }
//
//            try {
//                Thread.sleep(50);
//            } catch (InterruptedException e) {
//            }
//
//            resetGraphFlows(paths);
//
//            // Calculate the objective function - punish for bad point choice, reward for good point choice
//            // Objective: Minimize the variance or dissimilarity of costs
//            double totalCost = 0;
//            if (isGoalOptimum) {
//                totalCost = calculateOptimumObjectiveValue(costs, flows);
//            }
//            else {
//                totalCost = calculateNashObjectiveValue(costs, flows);
//            }
//
//            // Apply penalty for breaking the constraints
//            double penalty = 0;
//            if (Arrays.stream(flows).anyMatch(flow -> flow < 0.0)) {
//                penalty += Arrays.stream(flows).filter(flow -> flow < 0.0).map(flow -> flow * flow).sum(); // Quadratic penalty for negative flows
//            }
//            double sumFlows = Arrays.stream(flows).sum();
//            if (sumFlows > T) {
//                penalty += (sumFlows - T) * (sumFlows - T); // Quadratic penalty for exceeding T
//            }
//            totalCost += penalty;
//
//            return totalCost;
//        };
//
//        return function;
//    }
//
//    private void fillGraphFlows(List<FlowPath> paths, double[] flows) {
//        resetGraphFlows(paths);
//
//        for (int i = 0; i < paths.size(); i++) {
//            List<FlowEdge> edges = paths.get(i).getEdges();
//            for (FlowEdge edge : edges) {
//                edge.setCurrentFlow(edge.getCurrentFlow() + flows[i]);
//            }
//        }
//    }
//
//    private double calculateTotalCost(List<FlowPath> paths, double[] flows) {
//        fillGraphFlows(paths, flows);
//        double totalFlow = Arrays.stream(flows).sum();
//
//        double result = 0.0;
//        for (int i = 0; i < paths.size(); i++) {
//            if (totalFlow > 1.0) {
//                result += paths.get(i).getCurrentCost() * (flows[i] / totalFlow);
//            } else {
//                result += paths.get(i).getCurrentCost() * flows[i];
//            }
//        }
//
//        resetGraphFlows(paths);
//
//        return result;
//    }
//
//    private double[] calculateFlowsFromPoint(double totalFlow, double[] point) {
//        double[] flows = new double[point.length + 1];
//        double remainingFlow = totalFlow;
//
//        for (int i = 0; i < point.length; i++) {
//            flows[i] = point[i];
//            remainingFlow -= point[i];
//        }
//
//        flows[point.length] = remainingFlow;
//        return flows;
//    }
//
//    private double[] calculateIntegerFlowsFromPoint(double totalFlow, double[] point) {
//        double[] flows = new double[point.length + 1];
//        int totalIntegerFlow = (int) Math.round(totalFlow);
//        int sumOfFlows = 0;
//
//        for (int i = 0; i < point.length; i++) {
//            flows[i] = Math.ceil(point[i]);
//            sumOfFlows += flows[i];
//        }
//
//        // Adjust the last flow to ensure the sum equals totalFlow
//        flows[point.length] = totalIntegerFlow - sumOfFlows;
//
//        // Optional: Check if the adjustment made the last flow negative.
//        // If so, redistribute the flow.
//        if (flows[point.length] < 0) {
//            redistributeFlows(flows, totalIntegerFlow);
//        }
//
//        return flows;
//    }
//
//    private void redistributeFlows(double[] flows, int totalFlow) {
//        // Implementation of flow redistribution logic
//        // One simple approach could be to reduce other flows by 1 until the sum is balanced.
//        // This is a basic and naive approach, more sophisticated logic might be needed based on your specific requirements.
//        int sum = Arrays.stream(flows).mapToInt(d -> (int) d).sum();
//        for (int i = 0; i < flows.length - 1 && sum > totalFlow; i++) {
//            if (flows[i] > 0) {
//                flows[i]--;
//                sum--;
//            }
//        }
//        flows[flows.length - 1] = totalFlow - sum;
//    }
//
//    private void resetGraphFlows(List<FlowPath> paths) {
//        double zeroFlow = 0.0;
//
//        for (FlowPath path : paths) {
//            for (FlowEdge edge : path.getEdges()) {
//                edge.setCurrentFlow(zeroFlow);
//            }
//        }
//    }
//
//    public double calculateNashObjectiveValue(double[] costs, double[] flows) {
//        // minimizes the potential function
//        double resValue = 0;
//
//        for (int i = 0; i < costs.length; i++) {
//            resValue += costs[i];
//        }
//
//        return resValue;
//    }
//
//    public double calculateOptimumObjectiveValue(double[] costs, double[] flows) {
//        // minimizes the difference between the marginal costs
//        double mean = Arrays.stream(costs).average().orElse(0);
//        double sumOfSquaredDeviations = Arrays.stream(costs)
//                .map(cost -> Math.pow(cost - mean, 2))
//                .sum();
//
//        // The objective is to minimize this sum
//        return sumOfSquaredDeviations;
//    }
//}

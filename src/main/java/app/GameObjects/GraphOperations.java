package app.GameObjects;

import org.javatuples.Triplet;

import java.util.List;

public class GraphOperations {
    public static boolean checkNashEquil(List<Path> paths, List<List<Triplet<Double, Double, Double>>> matrix, List<List<Double>> distribution) {
        for (int i = 0; i < paths.size(); i++) {
            if (canBeAdjusted(i, paths, matrix, distribution)) {
                return false;
            }
        }
        return true;
    }

    private static boolean canBeAdjusted(int pathIndex, List<Path> paths, List<List<Triplet<Double, Double, Double>>> matrix, List<List<Double>> distribution) {
        addPayloadPath(-1.0, paths.get(pathIndex), distribution);
        double value = getPathValue(paths.get(pathIndex), matrix, distribution);
        for (int i = 0; i < paths.size(); i++) {
            if (pathIndex != i) {
                double valueCmp = getPathValue(paths.get(i), matrix, distribution);
                if (valueCmp < value) {
                    addPayloadPath(1.0, paths.get(pathIndex), distribution);
                    return true;
                }
            }
        }
        return false;
    }

    public static void addPayloadPath(double payload, Path path, List<List<Double>> distribution) {
        for (int i = 0; i < path.getRoute().size() - 1; i++) {
            addPayloadStartDest(payload, path.getRoute().get(i), path.getRoute().get(i + 1), distribution);
        }
        path.addPayload(payload);
    }

    public static double getPathValue(Path path, List<List<Triplet<Double, Double, Double>>> matrix, List<List<Double>> distribution) {
        double value = 0;
        List<Integer> stations = path.getRoute();
        for (int i = 0; i < stations.size() - 1; i++) {
            Triplet<Double, Double, Double> parameters = getConnection(stations.get(i), stations.get(i + 1), matrix);
            double payload = getDistribution(stations.get(i), stations.get(i + 1), distribution);
            value += Math.pow(payload, 2) * parameters.getValue0() + payload * parameters.getValue1() + parameters.getValue2();
        }
        return value;
    }

    private static void addPayloadStartDest(double payload, int start, int destination, List<List<Double>> distribution) {
        double current = distribution.get(start).get(destination);
        distribution.get(start).set(destination, current + payload);
    }

    private static Triplet<Double, Double, Double> getConnection(int start, int destination, List<List<Triplet<Double, Double, Double>>> matrix) {
        return matrix.get(start).get(destination);
    }

    private static double getDistribution(int start, int destination, List<List<Double>> distribution) {
        return distribution.get(start).get(destination);
    }

}

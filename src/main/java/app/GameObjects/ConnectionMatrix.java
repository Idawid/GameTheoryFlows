package app.GameObjects;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConnectionMatrix {
    private List<List<Triplet<Double, Double, Double>>> matrix;
    private final int numberOfVertices;
    private List<Path> paths;
    private List<List<Double>> distribution;

    public List<List<Triplet<Double, Double, Double>>> getMatrix() {
        return matrix;
    }

    public int getNumberOfVertices() {
        return numberOfVertices;
    }

    public List<List<Double>> getDistribution() {
        return distribution;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public ConnectionMatrix(ConnectionMatrix connectionMatrix) {
        this.matrix = connectionMatrix.getMatrix();
        this.numberOfVertices = connectionMatrix.getNumberOfVertices();
        this.distribution = connectionMatrix.getDistribution();
        this.paths = connectionMatrix.getPaths();
    }

    public ConnectionMatrix(int numberOfVertices) {
        this.paths = new ArrayList<>();
        this.numberOfVertices = numberOfVertices;
        this.matrix = new ArrayList<>();
        this.distribution = new ArrayList<>();
        for (int i = 0; i < this.numberOfVertices; i++) {
            List<Triplet<Double, Double, Double>> rowPaths = new ArrayList<>();
            List<Double> rowDistribution = new ArrayList<>();
            for (int j = 0; j < this.numberOfVertices; j++) {
                Triplet<Double, Double, Double> cell = new Triplet<Double, Double, Double>(0.0, 0.0, 0.0);
                rowPaths.add(cell);
                rowDistribution.add(0.0);
            }
            this.matrix.add(rowPaths);
            this.distribution.add(rowDistribution);
        }
    }

    private Triplet<Double, Double, Double> getConnection(int start, int destination) {
        return this.matrix.get(start).get(destination);
    }

    private double getDistribution(int start, int destination) {
        return this.distribution.get(start).get(destination);
    }

    public void addParameters(double a, double b, double c, int start, int destination) {
        this.matrix.get(start).add(destination, new Triplet<Double, Double, Double>(a, b, c));
    }

    private List<Integer> nextStations(int start) {
        List<Integer> indices = new ArrayList<>();
        if (start == -1) {
            indices.add(0);
            return indices;
        }
        List<Triplet<Double, Double, Double>> startList = this.matrix.get(start);
        for (int i = 0; i < this.numberOfVertices; i++) {
            Triplet<Double, Double, Double> test = startList.get(i);
            if (test.getValue0() != 0.0 || test.getValue1() != 0.0 || test.getValue2() != 0.0) {
                indices.add(i);
            }
        }
        return indices;
    }

    public void getAllPaths() {
        Path initialPath = new Path();
        int finish = this.numberOfVertices - 1;
        this.paths.clear();
        List<Integer> visited = new ArrayList<>();
        visited.add(0);
        addPath(initialPath, 0, finish, visited);
    }
    public double anarchyCost(double payload, double step){
        double bestSolution = bestGameValue(payload, step).getValue1();
        List<Double> nashEquils = getAllNashEquil(payload, step).getValue1();
        if (nashEquils.size() ==1){
            return nashEquils.get(0)/bestSolution;
        }
        double worstNashEquil = nashEquils.get(0);
        for(Double d : nashEquils){
            worstNashEquil = Math.max(worstNashEquil,d);
        }
        return worstNashEquil/bestSolution;
    }
    public double stabilityCost(double payload, double step){
        double bestSolution = bestGameValue(payload, step).getValue1();
        List<Double> nashEquils = getAllNashEquil(payload, step).getValue1();
        if (nashEquils.size() ==1){
            return nashEquils.get(0)/bestSolution;
        }
        double bestNashEquil = nashEquils.get(0);
        for(Double d : nashEquils){
            bestNashEquil = Math.min(bestNashEquil,d);
        }
        return bestNashEquil/bestSolution;
    }
    public void fixedPayload(double payload, double step) {
        resetPayload();
        double added = 0;
        if (paths.size() <= 0) {
            return;
        }
        if (paths.size() == 1) {
            addPayloadPath(payload, paths.get(0));
        }
        while (added < payload) {
            int index = 0;
            double value = getPathValue(paths.get(0));
            for (int i = 1; i < paths.size(); i++) {
                double valueNew = getPathValue(paths.get(i));
                if (valueNew < value) {
                    index = i;
                }
            }
            addPayloadPath(step, paths.get(index));
            added += step;
        }
    }

    private void addPath(Path path, int station, int finish, List<Integer> visited) {
        if (finish == station) {
            path.addStation(finish);
            this.paths.add(path);
        } else {
            List<Integer> neighbors = nextStations(station);
            for (Integer i : neighbors) {
                if (!visited.contains(i)) {
                    Path newPath = new Path(path.getRoute(), station);
                    List<Integer> visitedNew = new ArrayList<>(visited);
                    newPath.addStation(station);
                    addPath(newPath, i, finish, visitedNew);
                }
            }
        }
    }


    public double getPathValue(Path path) {
        double value = 0;
        List<Integer> stations = path.getRoute();
        for (int i = 0; i < stations.size() - 1; i++) {
            Triplet<Double, Double, Double> parameters = getConnection(stations.get(i), stations.get(i + 1));
            double payload = getDistribution(stations.get(i), stations.get(i + 1));
            value += Math.pow(payload, 2) * parameters.getValue0() + payload * parameters.getValue1() + parameters.getValue2();
        }
        return value;
    }

    //
    public void addPayloadPath(double payload, Path path) {
        for (int i = 0; i < path.getRoute().size() - 1; i++) {
            addPayloadStartDest(payload, path.getRoute().get(i), path.getRoute().get(i + 1));
        }
        path.addPayload(payload);
    }

    private void addPayloadStartDest(double payload, int start, int destination) {
        double current = this.distribution.get(start).get(destination);
        this.distribution.get(start).set(destination, current + payload);
    }

    private void movePayload(double payload, Path source, Path destination) {
        addPayloadPath(-payload, source);
        addPayloadPath(payload, destination);
    }

    public void payloadListDiscrete(List<Integer> list) throws Exception {
        if (list.size() != this.paths.size()) {
            throw new Exception("Provided list has different size than the list of all paths. List size : "
                    + list.size() + " Number of paths: " + this.paths.size());
        }
        for (int i = 0; i < list.size(); i++) {
            addPayloadPath(list.get(i), this.paths.get(i));
        }
    }

    public void randomPayloadDiscrete(double payload) {
        Random random = new Random();
        for (int i = 0; i < payload; i++) {
            int index = random.nextInt(this.paths.size());
            addPayloadPath(1, this.paths.get(index));
        }
    }

    public void randomPayloadContinuous(double payload) {
        Random random = new Random();
        while (payload > 0) {
            int index = random.nextInt(this.paths.size());
            double value = random.nextDouble(1.0);
            value = value <= payload ? value : payload;
            payload -= value;
            addPayloadPath(value, this.paths.get(index));
        }
    }

    public void printPayload() {
        for (int i = 0; i < paths.size(); i++) {
            System.out.println("Path " + i + " payload: " + paths.get(i).getPayload());
        }
        System.out.println("-------------------------");
    }

    public void printDistributions() {
        for (int i = 0; i < this.distribution.size(); i++) {
            for (int j = 0; j < this.distribution.get(i).size(); j++) {
                System.out.print(this.distribution.get(i).get(j) + "|");
            }
            System.out.println();
        }
        System.out.println("---------------------");
    }

    public boolean simulationStepDiscrete() {
        for (int i = 0; i < this.paths.size(); i++) {
            if (adjustPath(i, 1.0)) {
                return true;
            }
        }
        return false;
    }

    public boolean simulationStepContinuous(double step) {
        for (int i = 0; i < this.paths.size(); i++) {
            if (adjustPath(i, step)) {
                return true;
            }
        }
        return false;
    }

    private boolean canBeAdjusted(int pathIndex, double payload) {
        addPayloadPath(-payload, this.paths.get(pathIndex));
        double value = getPathValue(this.paths.get(pathIndex));
        for (int i = 0; i < paths.size(); i++) {
            if (pathIndex != i) {
                double valueCmp = getPathValue(this.paths.get(i));
                if (valueCmp < value) {
                    addPayloadPath(payload, this.paths.get(pathIndex));
                    return true;
                }
            }
        }
        addPayloadPath(payload, this.paths.get(pathIndex));
        return false;
    }

    public boolean adjustPath(int pathIndex, double payloadChange) {
        //check if the payload can be moved
        if (this.paths.get(pathIndex).getPayload() <= 0) {
            return false;
        }
        addPayloadPath(-payloadChange, this.paths.get(pathIndex));
        int index = pathIndex;
        double minPathVal = getPathValue(this.paths.get(pathIndex));
        for (int i = 0; i < this.paths.size(); i++) {
            double val = getPathValue(this.paths.get(i));
            if (val < minPathVal) {
                index = i;
                minPathVal = val;
            }
        }
        addPayloadPath(payloadChange, this.paths.get(index));
        return pathIndex != index;
    }

    public boolean isNashEquilibriumDiscrete() {
        for (int i = 0; i < this.paths.size(); i++) {
            if (canBeAdjusted(i, 1.0)) {
                return false;
            }
        }
        return true;
    }

    public boolean isNashEquilibriumContinuous(double precision) {
        for (int i = 0; i < this.paths.size(); i++) {
            if (canBeAdjusted(i, precision)) {
                return false;
            }
        }
        return true;
    }

    private void resetPayload() {
        for (Path path : paths) {
            addPayloadPath(-path.getPayload(), path);
        }
    }

    private double getGameValue() {
        double value = 0.0;
        for (Path path : paths) {
            value += getPathValue(path);
        }
        return value;
    }

    //finding all possible path distribution
    public Pair<List<List<Double>>, Double> bestGameValue(double payload, double step) {
        resetPayload();
        double min_value = Double.MAX_VALUE;
        List<List<Double>> distribution = new ArrayList<>();
        while (allPathsCheck(payload)) {
            addPayloadPath(step, this.paths.get(0));
            if (totalPayload() == payload) {
                double value = getGameValue();
                if (value < min_value) {
                    min_value = value;
                    distribution = getDistribution();
                }
            }
            if (totalPayload() >= payload) {
                for (int i = 0; i < this.paths.size() - 1; i++) {
                    if (this.paths.get(i).getPayload() >= payload) {
                        addPayloadPath(step, this.paths.get(i + 1));
                        addPayloadPath(-(this.paths.get(i).getPayload()), this.paths.get(i));
                    }
                }
            }
        }
        System.out.println("Done2");
        return new Pair<>(distribution, min_value);
    }

    public Pair<List<List<List<Double>>>, List<Double>> getAllNashEquil(double payload, double step) {
        resetPayload();
        List<Double> distributionValues = new ArrayList<>();
        List<List<List<Double>>> distributions = new ArrayList<>();
        while (allPathsCheck(payload)) {
            addPayloadPath(step, this.paths.get(0));
            if (totalPayload() == payload && isNashEquilibriumDiscrete()) {
                List<List<Double>> matrix = new ArrayList<>();
                for (int i = 0; i < this.numberOfVertices; i++) {
                    List<Double> row = new ArrayList<>();
                    for (int j = 0; j < this.numberOfVertices; j++) {
                        row.add(this.distribution.get(i).get(j));
                    }
                    matrix.add(row);
                }
                distributionValues.add(getGameValue());
                distributions.add(matrix);
            }
            if (totalPayload() >= payload) {
                for (int i = 0; i < this.paths.size() - 1; i++) {
                    if (this.paths.get(i).getPayload() >= payload) {
                        addPayloadPath(step, this.paths.get(i + 1));
                        addPayloadPath(-(this.paths.get(i).getPayload()), this.paths.get(i));
                    }
                }
            }
        }
        System.out.println("Done");
        return new Pair<>(distributions, distributionValues);
    }

    private boolean allPathsCheck(double payload) {
        return this.paths.get(this.paths.size() - 1).getPayload() <= payload;
    }

    private double totalPayload() {
        double sum = 0;
        for (Path p : this.paths) {
            sum += p.getPayload();
        }
        return sum;
    }
}

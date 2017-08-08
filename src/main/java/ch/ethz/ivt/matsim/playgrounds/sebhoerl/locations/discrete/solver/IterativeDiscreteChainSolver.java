package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.ContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.DiscreteLocation;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.discretizer.LocationDiscretizer;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver.DiscreteChainSolver;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class IterativeDiscreteChainSolver implements DiscreteChainSolver {
    final private ContinuousChainSolver chainSolver;
    final private long numberOfIterations;

    public class Result implements DiscreteSolver.Result {
        final private List<DiscreteLocation> discreteLocations;
        final private List<Double> discreteDistances;
        final private long iterations;
        final private boolean isConverged;

        public Result(List<DiscreteLocation> discreteLocations, List<Double> discreteDistances, long iterations, boolean isConverged) {
            this.discreteLocations = discreteLocations;
            this.discreteDistances = discreteDistances;
            this.iterations = iterations;
            this.isConverged = isConverged;
        }

        @Override
        public List<DiscreteLocation> getDiscreteLocations() {
            return discreteLocations;
        }

        @Override
        public List<Double> getDiscreteDistances() {
            return discreteDistances;
        }

        @Override
        public long getNumberOfIterations() {
            return iterations;
        }

        @Override
        public boolean isConverged() {
            return isConverged;
        }
    }

    public IterativeDiscreteChainSolver(ContinuousChainSolver chainSolver, long numberOfIterations) {
        this.chainSolver = chainSolver;
        this.numberOfIterations = numberOfIterations;
    }

    @Override
    public Result solve(Vector2D originLocation, Vector2D destinationLocation, List<Double> distances, List<Double> convergenceThresholds, List<LocationDiscretizer> discretizers) {
        double bestDifference = Double.POSITIVE_INFINITY;
        List<DiscreteLocation> bestLocations = null;

        long k = 0;
        boolean converged = false;

        while (k < numberOfIterations && !converged) {
            ContinuousChainSolver.Result result = chainSolver.solve(originLocation, destinationLocation, distances);

            List<DiscreteLocation> discreteLocations = new LinkedList<>();

            for (int i = 0; i < distances.size() - 1; i++) {
                discreteLocations.add(discretizers.get(i).findDiscreteLocation(result.getLocations().get(i)));
            }

            List<Vector2D> discretizedLocations = discreteLocations.stream().map(d -> d.getLocation()).collect(Collectors.toList());
            discretizedLocations.add(0, originLocation);
            discretizedLocations.add(destinationLocation);

            List<Double> discretizedDistances = new LinkedList<>();
            for (int i = 0; i < discretizedLocations.size() - 1; i++) {
                discretizedDistances.add(discretizedLocations.get(i).distance(discretizedLocations.get(i + 1)));
            }

            converged = true;

            List<Double> differences = new LinkedList<>();
            for (int i = 0; i < discretizedDistances.size(); i++) {
                double difference = Math.abs(distances.get(i) - discretizedDistances.get(i));
                differences.add(difference);
                converged &= difference < convergenceThresholds.get(i);
            }

            double totalDifference = differences.stream().reduce(0.0, Double::sum);

            if (totalDifference < bestDifference) {
                bestDifference = totalDifference;
                bestLocations = discreteLocations;
            }

            k++;
        }

        List<Vector2D> allLocations = new LinkedList<>(bestLocations.stream().map(l -> l.getLocation()).collect(Collectors.toList()));
        allLocations.add(0, originLocation);
        allLocations.add(destinationLocation);

        List<Double> bestDistances = new LinkedList<>();
        for (int i = 0; i < allLocations.size() - 1; i++) {
            bestDistances.add(allLocations.get(i).distance(allLocations.get(i + 1)));
        }

        return new Result(bestLocations, bestDistances, k, converged);
    }
}

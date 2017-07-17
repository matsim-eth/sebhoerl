package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation.ContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation.TailGenerator;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SamplingTailSolver {
    final private TailGenerator tailGenerator;
    final private long numberOfIterations;

    public class Result implements DiscreteChainSolver.Result {
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
        public long getIterations() {
            return iterations;
        }

        @Override
        public boolean isConverged() {
            return isConverged;
        }
    }

    public SamplingTailSolver(TailGenerator tailGenerator, long numberOfIterations) {
        this.tailGenerator = tailGenerator;
        this.numberOfIterations = numberOfIterations;
    }

    public SamplingTailSolver.Result findDiscreteLocations(Vector2D anchorLocation, List<Double> distances, List<Double> convergenceThresholds, List<LocationDiscretizer> discretizers) {
        double bestDifference = Double.POSITIVE_INFINITY;
        List<DiscreteLocation> bestLocations = null;

        long k = 0;
        boolean converged = false;

        while (k < numberOfIterations && !converged) {
            TailGenerator.Result result = tailGenerator.sample(anchorLocation, distances);

            List<DiscreteLocation> discreteLocations = new LinkedList<>();

            for (int i = 0; i < distances.size() - 1; i++) {
                discreteLocations.add(discretizers.get(i).findDiscreteLocation(result.getLocations().get(i)));
            }

            List<Vector2D> discretizedLocations = discreteLocations.stream().map(d -> d.getLocation()).collect(Collectors.toList());
            discretizedLocations.add(0, anchorLocation);

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
        allLocations.add(0, anchorLocation);

        List<Double> bestDistances = new LinkedList<>();
        for (int i = 0; i < allLocations.size() - 1; i++) {
            bestDistances.add(allLocations.get(i).distance(allLocations.get(i + 1)));
        }

        return new SamplingTailSolver.Result(bestLocations, bestDistances, k, converged);
    }
}

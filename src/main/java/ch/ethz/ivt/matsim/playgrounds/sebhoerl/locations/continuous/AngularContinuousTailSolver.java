package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AngularContinuousTailSolver implements ContinuousTailSolver {
    final private Random random;

    public class Result implements ContinuousSolver.Result {
        final private List<Vector2D> locations;
        final private List<Double> distances;

        public Result(List<Vector2D> locations, List<Double> distances) {
            this.locations = locations;
            this.distances = distances;
        }

        @Override
        public List<Vector2D> getLocations() {
            return locations;
        }

        @Override
        public List<Double> getDistances() {
            return distances;
        }

        @Override
        public boolean isConverged() {
            return true;
        }

        @Override
        public long getNumberOfIterations() {
            return 0;
        }
    }

    public AngularContinuousTailSolver(Random random) {
        this.random = random;
    }

    public AngularContinuousTailSolver() {
        this.random = null;
    }

    @Override
    public Result solve(Vector2D anchorLocation, List<Double> distances, List<Vector2D> initialLocations) {
        return solve(anchorLocation, distances);
    }

    @Override
    public Result solve(Vector2D anchorLocation, List<Double> distances) {
        List<Vector2D> locations = new LinkedList<>();
        Vector2D currentLocation = anchorLocation;

        for (double distance : distances) {
            double angle = random != null ? random.nextDouble() * Math.PI * 2.0 : 0.0;
            Vector2D newLocation = currentLocation.add(distance, new Vector2D(Math.cos(angle), Math.sin(angle)));
            locations.add(newLocation);
            currentLocation = newLocation;
        }

        return new Result(locations, distances);
    }
}

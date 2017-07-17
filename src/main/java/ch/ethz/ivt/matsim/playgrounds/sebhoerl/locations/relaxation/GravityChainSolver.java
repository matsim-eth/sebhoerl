package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.matsim.api.core.v01.Coord;

import java.util.*;
import java.util.stream.Collectors;

public class GravityChainSolver implements ContinuousChainSolver {
    final private Random random;
    final private double convergenceThreshold;
    final private long maximumIterations;
    final private double alpha;

    class Result implements ContinuousChainSolver.Result {
        final private List<Vector2D> locations;
        final boolean isSolved;
        final private long iterations;
        final private List<Double> distances;

        public Result(boolean isSolved, long iterations, List<Vector2D> locations, List<Double> distances) {
            this.isSolved = isSolved;
            this.locations = locations;
            this.iterations = iterations;
            this.distances = distances;
        }

        @Override
        public List<Vector2D> getLocations() {
            return locations;
        }

        @Override
        public boolean isSolved() {
            return isSolved;
        }

        @Override
        public long getIterations() {
            return iterations;
        }

        @Override
        public List<Double> getDistances() {
            return distances;
        }
    }

    public GravityChainSolver(Random random, double alpha, double convergenceThreshold, long maximumIterations) {
        this.random = random;
        this.convergenceThreshold = convergenceThreshold;
        this.maximumIterations = maximumIterations;
        this.alpha = alpha;
    }

    public GravityChainSolver(double alpha, double convergenceThreshold, long maximumIterations) {
        this(null, alpha, convergenceThreshold, maximumIterations);
    }

    private Result solveOneDistanceCase(Vector2D originLocation, Vector2D destinationLocation, double distance) {
        double actualDistance = originLocation.distance(destinationLocation);

        if (Math.abs(actualDistance - distance) < convergenceThreshold) {
            return new Result(true, 0, Collections.emptyList(), Collections.singletonList(actualDistance));
        } else {
            return new Result(false, 0, Collections.emptyList(), Collections.singletonList(actualDistance));
        }
    }

    private Result solveTwoDistanceCase(Vector2D originLocation, Vector2D destinationLocation, double originDistance, double destinationDistance) {
        double directDistance = originLocation.distance(destinationLocation);

        if (directDistance > originDistance + destinationDistance) {
            Vector2D location = originLocation.scalarMultiply(0.5).add(destinationLocation.scalarMultiply(0.5));
            return new Result(false, 0, Collections.singletonList(location), Arrays.asList(location.distance(originLocation), location.distance(destinationLocation)));
        } else if (directDistance < Math.abs(originDistance - destinationDistance)) {
            Vector2D location = originLocation.scalarMultiply(0.5).add(destinationLocation.scalarMultiply(0.5));
            return new Result(false, 0, Collections.singletonList(location), Arrays.asList(location.distance(originLocation), location.distance(destinationLocation)));
        } else if (directDistance == 0.0) {
            double alpha = (random == null) ? 0.0 : random.nextDouble() * 2.0 * Math.PI;
            double radius = originDistance == destinationDistance ? originDistance : 0.5 * originDistance + 0.5 * destinationDistance;
            Vector2D location = originLocation.add(new Vector2D(Math.cos(alpha), Math.sin(alpha)).scalarMultiply(radius));
            return new Result(originDistance == destinationDistance, 0, Collections.singletonList(location), Arrays.asList(location.distance(originLocation), location.distance(destinationLocation)));
        } else {
            double A = 0.5 * (Math.pow(originDistance, 2.0) - Math.pow(destinationDistance, 2.0) + Math.pow(directDistance, 2.0)) / directDistance;

            // The math.max here is added to solve numerical problems (negative root)
            double H = Math.sqrt(Math.max(0.0, Math.pow(originDistance, 2.0) - Math.pow(A, 2.0)));

            double r = (random == null) ? 0.0 : random.nextDouble();

            Vector2D center = originLocation.add(destinationLocation.subtract(originLocation).scalarMultiply(A / directDistance));
            Vector2D offset = destinationLocation.subtract(originLocation).scalarMultiply(H / directDistance);
            offset = new Vector2D(-offset.getY(), offset.getX());

            Vector2D location = (r < 0.5) ? center.add(offset) : center.add(-1.0, offset);

            return new Result(true, 0, Collections.singletonList(location), Arrays.asList(location.distance(originLocation), location.distance(destinationLocation)));
        }
    }

    @Override
    public Result solve(Vector2D originLocation, Vector2D destinationLocation, List<Double> distances, List<Vector2D> initialLocations) {
        if (distances.size() == 0) {
            return new Result(false, 0, Collections.emptyList(), Collections.emptyList());
        } else if (distances.size() == 1) {
            return solveOneDistanceCase(originLocation, destinationLocation, distances.get(0));
        } else if (distances.size() == 2) {
            return solveTwoDistanceCase(originLocation, destinationLocation, distances.get(0), distances.get(1));
        }

        List<Vector2D> locations = initialLocations.stream().map(v -> new Vector2D(1.0, v)).collect(Collectors.toList());

        locations.add(0, originLocation);
        locations.add(destinationLocation);

        long k = 0;
        boolean solved = false;

        while (k < maximumIterations && !solved) {
            List<Vector2D> newLocations = new LinkedList<>();
            newLocations.add(originLocation);

            for (int index = 1; index < distances.size(); index++) {
                Vector2D currentLocation = locations.get(index);
                Vector2D previousLocation = locations.get(index - 1);
                Vector2D nextLocation = locations.get(index + 1);

                double expectedPreviousDistance = distances.get(index - 1);
                double expectedNextDistance = distances.get(index);

                double previousDistance = previousLocation.distance(currentLocation);
                double nextDistance = nextLocation.distance(currentLocation);

                double previousDifference = expectedPreviousDistance - previousDistance;
                double nextDifference = expectedNextDistance - nextDistance;

                Vector2D updateFromPrevious = previousLocation.subtract(currentLocation).normalize().scalarMultiply(-alpha * previousDifference);
                Vector2D updateFromNext = nextLocation.subtract(currentLocation).normalize().scalarMultiply(-alpha * nextDifference);

                Vector2D update = updateFromPrevious.scalarMultiply(0.5).add(updateFromNext.scalarMultiply(0.5));
                newLocations.add(currentLocation.add(update));
            }

            newLocations.add(destinationLocation);
            locations = newLocations;

            double totalDifference = 0.0;

            for (int i = 0; i < distances.size(); i++) {
                totalDifference += Math.abs(locations.get(i).distance(locations.get(i + 1)) - distances.get(i));
            }

            if (totalDifference <= convergenceThreshold) {
                solved = true;
            }

            k++;
        }

        List<Double> resultDistances = new LinkedList<>();

        for (int i = 0; i < locations.size() - 1; i++) {
            resultDistances.add(locations.get(i).distance(locations.get(i + 1)));
        }

        return new Result(solved, k, locations.subList(1, locations.size() - 1), resultDistances);
    }

    @Override
    public Result solve(Vector2D origin, Vector2D destination, List<Double> distances) {
        Vector2D difference = destination.subtract(origin);
        Vector2D direction = difference.getNorm() > 0.0 ? difference.normalize() : new Vector2D(0.1, 0.1);
        double directDistance = destination.distance(origin);

        double totalDistance = distances.stream().mapToDouble(d -> d).sum();
        double cumulativeDistance = 0.0;

        List<Vector2D> initialLocations = new LinkedList<>();

        for (int i = 0; i < distances.size() - 1; i++) {
            cumulativeDistance += distances.get(i);
            initialLocations.add(origin.add(directDistance * (cumulativeDistance / totalDistance), direction));
        }

        if (this.random != null) {
            for (int i = 0; i < initialLocations.size(); i++) {
                initialLocations.set(i, initialLocations.get(i).add(new Vector2D(random.nextGaussian(), random.nextGaussian())));
            }
        }

        return solve(origin, destination, distances, initialLocations);
    }
}

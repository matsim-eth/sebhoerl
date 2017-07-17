package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;

public interface ContinuousChainSolver {
    interface Result {
        boolean isSolved();
        List<Vector2D> getLocations();
        List<Double> getDistances();
        long getIterations();
    }

    Result solve(Vector2D originLocation, Vector2D destinationLocation, List<Double> distances, List<Vector2D> initialLocations);
    Result solve(Vector2D originLocation, Vector2D destinationLocation, List<Double> distances);
}

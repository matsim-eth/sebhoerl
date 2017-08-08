package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;

public interface ContinuousChainSolver extends ContinuousSolver {
    Result solve(Vector2D originLocation, Vector2D destinationLocation, List<Double> distances, List<Vector2D> initialLocations);
    Result solve(Vector2D originLocation, Vector2D destinationLocation, List<Double> distances);
}

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;

public interface DiscreteChainSolver {
    interface Result {
        List<DiscreteLocation> getDiscreteLocations();
        List<Double> getDiscreteDistances();
        long getIterations();
        boolean isConverged();
    }

    Result findDiscreteLocations(Vector2D originLocation, Vector2D destinationLocation, List<Double> distances, List<Double> convergenceThresholds, List<LocationDiscretizer> discretizers);
}

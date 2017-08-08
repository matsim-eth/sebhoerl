package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;

public interface ContinuousSolver {
    interface Result {
        List<Vector2D> getLocations();
        List<Double> getDistances();

        boolean isConverged();
        long getNumberOfIterations();
    }
}

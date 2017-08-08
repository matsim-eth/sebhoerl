package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.discretizer;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.ContinuousChainSolver;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;

class MockContinuousChainSolverResult implements ContinuousChainSolver.Result {
    final private List<Vector2D> locations;
    final private List<Double> distances;
    final private boolean isConverged;
    final private long numberOfIterations;

    public MockContinuousChainSolverResult(List<Vector2D> locations, List<Double> distances, boolean isConverged, long numberOfIterations) {
        this.locations = locations;
        this.distances = distances;
        this.isConverged = isConverged;
        this.numberOfIterations = numberOfIterations;
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
        return isConverged;
    }

    @Override
    public long getNumberOfIterations() {
        return numberOfIterations;
    }
}
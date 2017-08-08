package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.discretizer;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.ContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.ContinuousSolver;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.LinkedList;
import java.util.List;

public class MockContinuousChainSolver implements ContinuousChainSolver {
    final private List<MockContinuousChainSolverResult> results = new LinkedList<>();

    public void addResult(MockContinuousChainSolverResult result) {
        results.add(result);
    }

    @Override
    public Result solve(Vector2D originLocation, Vector2D destinationLocation, List<Double> distances, List<Vector2D> initialLocations) {
        return results.remove(0);
    }

    @Override
    public Result solve(Vector2D originLocation, Vector2D destinationLocation, List<Double> distances) {
        return results.remove(0);
    }
}

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous;

import com.google.common.collect.Lists;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;

public class ReversedTailSolverResult implements ContinuousTailSolver.Result {
    final private ContinuousTailSolver.Result delegate;

    public ReversedTailSolverResult(ContinuousTailSolver.Result delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<Vector2D> getLocations() {
        return Lists.reverse(delegate.getLocations());
    }

    @Override
    public List<Double> getDistances() {
        return Lists.reverse(delegate.getDistances());
    }

    @Override
    public boolean isConverged() {
        return delegate.isConverged();
    }

    @Override
    public long getNumberOfIterations() {
        return delegate.getNumberOfIterations();
    }

    public ContinuousTailSolver.Result getOriginalResult() {
        return delegate;
    }
}

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.ReversedTailSolverResult;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.DiscreteLocation;
import com.google.common.collect.Lists;

import java.util.List;

public class ReversedDiscreteTailSolverResult implements DiscreteTailSolver.Result {
    final private DiscreteTailSolver.Result delegate;

    public ReversedDiscreteTailSolverResult(DiscreteTailSolver.Result delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<DiscreteLocation> getDiscreteLocations() {
        return Lists.reverse(delegate.getDiscreteLocations());
    }

    @Override
    public List<Double> getDiscreteDistances() {
        return Lists.reverse(delegate.getDiscreteDistances());
    }

    @Override
    public long getNumberOfIterations() {
        return delegate.getNumberOfIterations();
    }

    @Override
    public boolean isConverged() {
        return delegate.isConverged();
    }
}

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.ContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.ContinuousSolver;

public class ChainSamplerResult {
    final private ContinuousSolver.Result continuousSolverResult;
    final long numberOfSamples;
    final long numberOfSolverIterations;
    final boolean isConverged;

    public ChainSamplerResult(boolean isConverged, ContinuousSolver.Result continuousSolverResult, long numberOfSamples, long numberOfSolverIterations) {
        this.continuousSolverResult = continuousSolverResult;
        this.numberOfSamples = numberOfSamples;
        this.numberOfSolverIterations = numberOfSolverIterations;
        this.isConverged = isConverged;
    }

    public ContinuousChainSolver.Result getContinuousSolverResult() {
        return continuousSolverResult;
    }

    public long getNumberOfSolverIterations() {
        return numberOfSolverIterations;
    }

    public long getNumberOfSamples() {
        return numberOfSamples;
    }

    public boolean isConverged() {
        return isConverged;
    }
}

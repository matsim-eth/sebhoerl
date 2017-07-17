package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation.ContinuousChainSolver;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;
import java.util.stream.Collectors;

public class DistanceChainSampler {
    final private ContinuousChainSolver chainSolver;
    final private long maximumIterations;

    public class Result {
        final private ContinuousChainSolver.Result chainResult;
        final long samplingIterations;
        final long totalSolverIterations;
        final boolean isValid;

        public Result(boolean isValid, ContinuousChainSolver.Result chainResult, long samplingIterations, long totalSolverIterations) {
            this.chainResult = chainResult;
            this.samplingIterations = samplingIterations;
            this.totalSolverIterations = totalSolverIterations;
            this.isValid = isValid;
        }

        public ContinuousChainSolver.Result getChainResult() {
            return chainResult;
        }

        public long getSamplingIterations() {
            return samplingIterations;
        }

        public long getTotalSolverIterations() {
            return totalSolverIterations;
        }

        public boolean isValid() {
            return isValid;
        }
    }

    public DistanceChainSampler(ContinuousChainSolver solver, long maximumIterations) {
        this.chainSolver = solver;
        this.maximumIterations = maximumIterations;
    }

    public Result sample(Vector2D originLocation, Vector2D destinationLocation, List<DistanceDistribution> distributions) {
        int k = 0;
        long solverIterations = 0;

        ContinuousChainSolver.Result result = null;

        while (k < maximumIterations) {
            List<Double> distances = distributions.stream().map(d -> d.sample()).collect(Collectors.toList());

            if (originLocation.equals(destinationLocation) && distributions.size() == 2) {
                // If there is a line, ie. one tour going back to the same location,
                // the distance should be the same (assuming there is no detour, but
                // this is not relevant for crowfly distances)
                // The chainSolver would only converge if the distances are exactly the
                // same, which has nearly zero probability.
                distances.set(1, distances.get(0));
            }

            result = chainSolver.solve(originLocation, destinationLocation, distances);

            k++;
            solverIterations += result.getIterations();

            if (result.isSolved()) {
                return new Result(true, result, k, solverIterations);
            }
        }

        return new Result(false, result, k, solverIterations);
    }
}

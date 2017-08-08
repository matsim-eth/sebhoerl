package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.ContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances.DistanceDistribution;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;
import java.util.stream.Collectors;

public class ChainSampler {
    final private ContinuousChainSolver solver;
    final private long maximumNumberOfSamples;

    public ChainSampler(ContinuousChainSolver solver, long maximumNumberOfSamples) {
        this.solver = solver;
        this.maximumNumberOfSamples = maximumNumberOfSamples;
    }

    public ChainSamplerResult sample(Vector2D originLocation, Vector2D destinationLocation, List<DistanceDistribution> distributions) {
        int samples = 0;
        long solverIterations = 0;

        ContinuousChainSolver.Result result = null;

        while (samples < maximumNumberOfSamples) {
            List<Double> distances = distributions.stream().map(d -> d.sample()).collect(Collectors.toList());

            if (originLocation.equals(destinationLocation) && distributions.size() == 2) {
                // If there is a line, ie. one tour going back to the same location,
                // the distances should be the same (assuming there is no detour, but
                // this is not relevant for crowfly distances)
                // The chainSolver would only converge if the distances are exactly the
                // same, which has nearly zero probability.
                distances.set(1, distances.get(0));
            }

            result = solver.solve(originLocation, destinationLocation, distances);

            samples++;
            solverIterations += result.getNumberOfIterations();

            if (result.isConverged()) {
                return new ChainSamplerResult(true, result, samples, solverIterations);
            }
        }

        return new ChainSamplerResult(false, result, samples, solverIterations);
    }
}

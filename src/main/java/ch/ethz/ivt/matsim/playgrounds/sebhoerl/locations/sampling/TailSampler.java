package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.ContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.ContinuousTailSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances.DistanceDistribution;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;
import java.util.stream.Collectors;

public class TailSampler {
    final private ContinuousTailSolver solver;
    final private long maximumNumberOfSamples;

    public TailSampler(ContinuousTailSolver solver, long maximumNumberOfSamples) {
        this.solver = solver;
        this.maximumNumberOfSamples = maximumNumberOfSamples;
    }

    public ChainSamplerResult sample(Vector2D anchorLocation, List<DistanceDistribution> distributions) {
        int samples = 0;
        long solverIterations = 0;

        ContinuousChainSolver.Result result = null;

        while (samples < maximumNumberOfSamples) {
            List<Double> distances = distributions.stream().map(d -> d.sample()).collect(Collectors.toList());
            result = solver.solve(anchorLocation, distances);

            samples++;
            solverIterations += result.getNumberOfIterations();

            if (result.isConverged()) {
                return new ChainSamplerResult(true, result, samples, solverIterations);
            }
        }

        return new ChainSamplerResult(false, result, samples, solverIterations);
    }
}

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation.ContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation.TailGenerator;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;
import java.util.stream.Collectors;

public class TailChainSampler {
    final private TailGenerator tailGenerator;

    public TailChainSampler(TailGenerator tailGenerator) {
        this.tailGenerator = tailGenerator;
    }

    public TailGenerator.Result sample(Vector2D anchorLocation, List<DistanceDistribution> distributions) {
        List<Double> distances = distributions.stream().map(d -> d.sample()).collect(Collectors.toList());
        return tailGenerator.sample(anchorLocation, distances);
    }
}

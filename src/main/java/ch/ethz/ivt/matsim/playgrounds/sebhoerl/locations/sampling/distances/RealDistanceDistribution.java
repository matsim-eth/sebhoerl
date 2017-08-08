package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances;

import org.apache.commons.math3.distribution.RealDistribution;

public class RealDistanceDistribution implements DistanceDistribution {
    final private RealDistribution distribution;

    public RealDistanceDistribution(RealDistribution distribution) {
        this.distribution = distribution;
    }

    @Override
    public double sample() {
        return distribution.sample();
    }
}

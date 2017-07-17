package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling;

import org.apache.commons.math3.distribution.RealDistribution;

public class DistanceDistributionAdapter implements DistanceDistribution {
    final private RealDistribution distribution;

    public DistanceDistributionAdapter(RealDistribution distribution) {
        this.distribution = distribution;
    }

    @Override
    public double sample() {
        return distribution.sample();
    }
}

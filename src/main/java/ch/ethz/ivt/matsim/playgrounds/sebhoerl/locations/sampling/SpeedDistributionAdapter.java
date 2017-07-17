package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling;

import org.apache.commons.math3.distribution.RealDistribution;

public class SpeedDistributionAdapter {
    final private RealDistribution speedDistribution;

    public SpeedDistributionAdapter(RealDistribution speedDistribution) {
        this.speedDistribution = speedDistribution;
    }

    public DistanceDistribution createDistribution(double travelTime) {
        return new DistanceDistribution() {
            @Override
            public double sample() {
                return 1000.0 * speedDistribution.sample() * (travelTime / 3600.0);
            }
        };
    }
}

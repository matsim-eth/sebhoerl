package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances;

import org.apache.commons.math3.distribution.NormalDistribution;

public class TruncatedNormalDistanceDistribution extends RealDistanceDistribution {
    public TruncatedNormalDistanceDistribution(double mu, double stddev) {
        super(new NormalDistribution(mu, stddev));
    }

    @Override
    public double sample() {
        double value = -1.0;

        do {
            value = super.sample();
        } while (value < 0.0);

        return value;
    }
}

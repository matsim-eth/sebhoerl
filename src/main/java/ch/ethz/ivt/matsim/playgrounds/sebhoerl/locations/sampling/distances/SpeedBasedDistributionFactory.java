package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances;

import org.apache.commons.math3.distribution.RealDistribution;

public class SpeedBasedDistributionFactory implements DistanceDistributionFactory {
    final private RealDistribution speedDistribution;

    public SpeedBasedDistributionFactory(RealDistribution speedDistribution) {
        this.speedDistribution = speedDistribution;
    }

    public class TravelTime implements TripCharacteristics {
        final private double travelTime;

        public TravelTime(double travelTime) {
            this.travelTime = travelTime;
        }

        public double getTravelTime() {
            return travelTime;
        }
    }

    @Override
    public DistanceDistribution createDistribution(TripCharacteristics tripCharacteristics) {
        if (!(tripCharacteristics instanceof TravelTime)) throw new IllegalArgumentException();

        return new DistanceDistribution() {
            @Override
            public double sample() {
                return 1000.0 * speedDistribution.sample() * (((TravelTime) tripCharacteristics).getTravelTime() / 3600.0);
            }
        };
    }
}

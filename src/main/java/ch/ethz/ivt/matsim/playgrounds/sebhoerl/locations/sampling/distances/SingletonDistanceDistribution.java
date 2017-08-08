package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances;

public class SingletonDistanceDistribution implements DistanceDistribution {
    final private double singletonValue;

    public SingletonDistanceDistribution(double singletonValue) {
        this.singletonValue = singletonValue;
    }

    @Override
    public double sample() {
        return singletonValue;
    }
}

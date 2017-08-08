package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.microcensus;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances.DistanceDistribution;

public class MaximumSpeedDistribution implements DistanceDistribution{
    final private DistanceDistribution delegate;
    final private double maximumSpeed;
    final private double travelTime;
    final private long maximumNumberOfIterations;

    public MaximumSpeedDistribution(DistanceDistribution delegate, double traveTime, double maximumSpeed, long maximumNumberOfIterations) {
        this.delegate = delegate;
        this.maximumSpeed = maximumSpeed;
        this.travelTime = traveTime;
        this.maximumNumberOfIterations = maximumNumberOfIterations;
    }

    @Override
    public double sample() {
        double distance = Double.POSITIVE_INFINITY;
        long iteraton = 0;

        while (distance / travelTime > maximumSpeed && iteraton < maximumNumberOfIterations) {
            distance = delegate.sample();
            iteraton++;
        }

        return distance;
    }
}

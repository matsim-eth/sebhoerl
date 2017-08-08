package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.discretizer.LocationDiscretizer;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances.DistanceDistribution;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class BasicInputChainElement implements InputChainElement {
    final private DistanceDistribution distanceDistribution;
    final private LocationDiscretizer locationDiscretizer;
    final private double discretizationThreshold;
    final private Vector2D originLocation;
    final private Vector2D destinationLocation;

    public BasicInputChainElement(Vector2D originLocation, Vector2D destinationLocation, DistanceDistribution distanceDistribution, LocationDiscretizer locationDiscretizer, double discretizationThreshold) {
        this.distanceDistribution = distanceDistribution;
        this.locationDiscretizer = locationDiscretizer;
        this.discretizationThreshold = discretizationThreshold;
        this.originLocation = originLocation;
        this.destinationLocation = destinationLocation;
    }

    @Override
    public DistanceDistribution getDistanceDistribution() {
        return distanceDistribution;
    }

    @Override
    public LocationDiscretizer getLocationDiscretizer() {
        return locationDiscretizer;
    }

    @Override
    public double getDiscretizationThreshold() {
        return discretizationThreshold;
    }

    @Override
    public Vector2D getOriginLocation() {
        return originLocation;
    }

    @Override
    public Vector2D getDestinationLocation() {
        return destinationLocation;
    }
}

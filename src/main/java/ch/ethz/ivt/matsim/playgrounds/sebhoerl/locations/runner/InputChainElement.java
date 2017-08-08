package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.discretizer.LocationDiscretizer;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances.DistanceDistribution;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public interface InputChainElement {
    DistanceDistribution getDistanceDistribution();
    LocationDiscretizer getLocationDiscretizer();
    double getDiscretizationThreshold();

    Vector2D getOriginLocation();
    Vector2D getDestinationLocation();
}

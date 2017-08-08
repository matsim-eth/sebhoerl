package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.discretizer;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.BasicDiscreteLocation;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.DiscreteLocation;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class ContinuousDiscretizer implements LocationDiscretizer {
    @Override
    public DiscreteLocation findDiscreteLocation(Vector2D location) {
        return new BasicDiscreteLocation(location);
    }
}

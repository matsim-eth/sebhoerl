package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public interface LocationDiscretizer {
    DiscreteLocation findDiscreteLocation(Vector2D location);
}

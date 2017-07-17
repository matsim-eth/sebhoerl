package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class BasicDiscreteLocation implements DiscreteLocation {
    final private Vector2D location;

    public BasicDiscreteLocation(Vector2D location) {
        this.location = location;
    }

    @Override
    public Vector2D getLocation() {
        return location;
    }
}

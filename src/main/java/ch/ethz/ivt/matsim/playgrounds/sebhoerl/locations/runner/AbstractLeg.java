package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public abstract class AbstractLeg implements InputChainElement {
    final private double referenceDistance;
    final private Vector2D originLocation;
    final private Vector2D destinationLocation;

    public AbstractLeg(double referenceDistance, Vector2D originLocation, Vector2D destinationLocation) {
        this.referenceDistance = referenceDistance;
        this.originLocation = originLocation;
        this.destinationLocation = destinationLocation;
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

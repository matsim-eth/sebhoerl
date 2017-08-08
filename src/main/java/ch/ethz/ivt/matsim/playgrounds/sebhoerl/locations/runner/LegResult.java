package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.DiscreteLocation;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class LegResult {
    final private InputChainElement originalLeg;

    final private double sampledDistance;
    final private double discretizedDistance;

    final private DiscreteLocation discreteOriginLocation;
    final private DiscreteLocation discreteDestinationLocation;

    final private Vector2D sampledOriginLocation;
    final private Vector2D sampledDestinationLocation;

    public LegResult(InputChainElement originalLeg, DiscreteLocation discreteOriginLocation, DiscreteLocation discreteDestinationLocation, Vector2D sampledOriginLocation, Vector2D sampledDestinationLocation, double sampledDistance, double discretizedDistance) {
        this.originalLeg = originalLeg;
        this.sampledDistance = sampledDistance;
        this.discretizedDistance = discretizedDistance;

        this.sampledOriginLocation = sampledOriginLocation;
        this.sampledDestinationLocation = sampledDestinationLocation;
        this.discreteOriginLocation = discreteOriginLocation;
        this.discreteDestinationLocation = discreteDestinationLocation;
    }

    public InputChainElement getOriginalLeg() {
        return originalLeg;
    }

    public double getSampledDistance() {
        return sampledDistance;
    }

    public double getDiscreteDistance() {
        return discretizedDistance;
    }

    public DiscreteLocation getDiscreteOriginLocation() {
        return discreteOriginLocation;
    }

    public DiscreteLocation getDiscreteDestinationLocation() {
        return discreteDestinationLocation;
    }

    public Vector2D getSampledOriginLocation() {
        return sampledOriginLocation;
    }

    public Vector2D getSampledDestinationLocation() {
        return sampledDestinationLocation;
    }
}

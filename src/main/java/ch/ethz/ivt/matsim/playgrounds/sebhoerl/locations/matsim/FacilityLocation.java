package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.BasicDiscreteLocation;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.matsim.facilities.ActivityFacility;

public class FacilityLocation extends BasicDiscreteLocation {
    final private ActivityFacility facility;

    public FacilityLocation(ActivityFacility facility) {
        super(new Vector2D(facility.getCoord().getX(), facility.getCoord().getY()));
        this.facility = facility;
    }

    public ActivityFacility getFacility() {
        return facility;
    }
}

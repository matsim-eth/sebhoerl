package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.List;

public class TripChain {
    final private List<TripStructureUtils.Trip> trips;
    final private double directDistance;
    final private Activity originActivity;
    final private Activity destinatonActivity;

    final private boolean isVariableBeginning;
    final private boolean isVariableEnd;
    final private boolean isClosed;

    public TripChain(List<TripStructureUtils.Trip> trips, boolean isVariableBeginning, boolean isVariableEnd) {
        this.originActivity = trips.get(0).getOriginActivity();
        this.destinatonActivity = trips.get(trips.size() - 1).getDestinationActivity();

        this.directDistance = CoordUtils.calcEuclideanDistance(originActivity.getCoord(), destinatonActivity.getCoord());
        this.trips = trips;

        this.isVariableBeginning = isVariableEnd;
        this.isVariableEnd = isVariableEnd;

        this.isClosed = originActivity.getCoord().equals(destinatonActivity.getCoord());
    }

    public Activity getOriginActivity() {
        return originActivity;
    }

    public Activity getDestinatonActivity() {
        return destinatonActivity;
    }

    public double getDirectDistance() {
        return directDistance;
    }

    public List<TripStructureUtils.Trip> getTrips() {
        return trips;
    }

    public boolean isVariableBeginning() {
        return isVariableBeginning;
    }

    public boolean isVariableEnd() {
        return isVariableEnd;
    }

    public boolean isClosed() {
        return isClosed;
    }
}
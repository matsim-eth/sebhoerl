package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.trip_chain;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Activity> getActivities() {
        List<Activity> activities = new LinkedList<>(trips.stream().map(t -> t.getOriginActivity()).collect(Collectors.toList()));
        activities.add(trips.get(trips.size() - 1).getDestinationActivity());
        return activities;
    }

    public List<Leg> getLegs() {
        List<Leg> legs = new LinkedList<>(trips.stream().map(t -> t.getLegsOnly().get(0)).collect(Collectors.toList()));
        return legs;
    }
}
package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim;

import java.util.*;

public class CapacityAwareFacilityDiscretizer extends FacilityDiscretizer {
    final private Set<FacilityLocation> deactivatedLocations = new HashSet<>();
    final private Map<FacilityLocation, List<Integer>> occupancy = new HashMap<>();

    final private double startTime;
    final private double endTime;
    final private int numberOfBins;

    public CapacityAwareFacilityDiscretizer(double startTime, double endTime, double occupancyInterval, Random random, double radius, Collection<FacilityLocation> facilities) {
        super(random, radius, facilities);

        this.startTime = startTime;
        this.endTime = endTime;
        this.numberOfBins = (int) Math.floor((endTime - startTime) / occupancyInterval);

        for (FacilityLocation location : facilities) {
            occupancy.put(location, Collections.nCopies(numberOfBins, 0));
        }
    }

    private int getOccupancyBin(double time) {
        return (int) Math.floor(numberOfBins * (time - startTime) / (endTime - startTime));
    }

    public void updateOccupancy(FacilityLocation location, double time, int amount) {
        int index = getOccupancyBin(time);
        occupancy.get(location).set(index, occupancy.get(location).get(index) + amount);
    }

    public void updateSearchSpace() {
        for (FacilityLocation location : deactivatedLocations) {
            quadtree.put(location.getLocation().getX(), location.getLocation().getY(), location);
        }

        deactivatedLocations.clear();

        /*for (FacilityLocation location : occupancy.keySet()) {
            
        }*/
    }
}

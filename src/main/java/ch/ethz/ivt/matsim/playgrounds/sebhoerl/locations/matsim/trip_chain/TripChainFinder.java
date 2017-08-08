package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.trip_chain;

import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.TripStructureUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TripChainFinder {
    final private StageActivityTypes stageActivityTypes;
    final private Set<String> variableActivityTypes;

    public TripChainFinder(Set<String> variableActivityTypes, StageActivityTypes stageActivityTypes) {
        this.stageActivityTypes = stageActivityTypes;
        this.variableActivityTypes = variableActivityTypes;
    }

    public List<TripChain> findTripChains(Plan plan) {
        List<TripStructureUtils.Trip> trips = TripStructureUtils.getTrips(plan.getPlanElements(), stageActivityTypes);
        List<TripChain> tripChains = new LinkedList<>();
        List<TripStructureUtils.Trip> chainElements = new LinkedList<>();

        for (TripStructureUtils.Trip trip : trips) {
            chainElements.add(trip);

            if (!variableActivityTypes.contains(trip.getDestinationActivity().getType())) {
                tripChains.add(new TripChain(new LinkedList<>(chainElements), variableActivityTypes.contains(chainElements.get(0).getOriginActivity().getType()), false));
                chainElements.clear();
            }
        }

        if (chainElements.size() > 0) {
            tripChains.add(new TripChain(new LinkedList<>(chainElements), false, variableActivityTypes.contains(chainElements.get(chainElements.size() - 1).getDestinationActivity().getType())));
        }

        return tripChains;
    }
}

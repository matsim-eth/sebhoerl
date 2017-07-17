package ch.ethz.ivt.matsim.playgrounds.sebhoerl.network_compression;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Counter;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityFacilityImpl;
import org.matsim.facilities.FacilitiesWriter;
import org.matsim.facilities.MatsimFacilitiesReader;
import org.matsim.pt.transitSchedule.api.*;

import java.util.*;
import java.util.stream.Collectors;

public class ScenarioAdjustment {
    final static private Logger logger = Logger.getLogger(ScenarioAdjustment.class);

    static public void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

        new MatsimNetworkReader(scenario.getNetwork()).readFile(args[0]);
        new TransitScheduleReader(scenario).readFile(args[1]);
        new PopulationReader(scenario).readFile(args[2]);
        new MatsimFacilitiesReader(scenario).readFile(args[3]);

        new ScenarioAdjustment(scenario).adjustScenario();

        new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(args[4]);
        new PopulationWriter(scenario.getPopulation()).write(args[5]);
        new FacilitiesWriter(scenario.getActivityFacilities()).write(args[6]);
    }

    final private Map<Id<Link>, Id<Link>> linkReplacements;
    final private Scenario scenario;

    public ScenarioAdjustment(Scenario scenario) {
        this.linkReplacements = findLinkReplacements(scenario.getNetwork());
        this.scenario = scenario;
    }

    private Map<Id<Link>, Id<Link>> findLinkReplacements(Network network) {
        logger.info("Finding link replacements ...");

        Map<Id<Link>, Id<Link>> linkReplacements = new HashMap<>();
        Counter counter = new Counter("", " links procesed");

        for (Link link : network.getLinks().values()) {
            String replacements = (String) link.getAttributes().getAttribute("replaces");

            if (replacements != null) {
                for (String replacedId : replacements.split(",")) {
                    linkReplacements.put(Id.createLinkId(replacedId), link.getId());
                }
            }

            counter.incCounter();
        }

        logger.info("Number of link replacements: " + linkReplacements.size());

        return linkReplacements;
    }

    public void adjustScenario() {
        adjustTransitSchedule();
        adjustFacilities();
        adjustPopulation();
    }

    private void adjustFacilities() {
        logger.info("Updating facilities ...");

        Counter counter = new Counter("", " facilities processed");

        long numberOfFacilities = 0;
        long numberOfAdjustedFacilities = 0;

        for (ActivityFacility facility : scenario.getActivityFacilities().getFacilities().values()) {
            if (facility instanceof ActivityFacilityImpl) {
                ActivityFacilityImpl impl = (ActivityFacilityImpl) facility;

                if (linkReplacements.containsKey(impl.getLinkId())) {
                    impl.setLinkId(linkReplacements.get(impl.getLinkId()));
                    //impl.setCoord(scenario.getNetwork().getLinks().get(impl.getLinkId()).getCoord());
                    numberOfAdjustedFacilities++;
                }
            }

            numberOfFacilities++;
            counter.incCounter();
        }

        logger.info(String.format("Number of facilities: %d", numberOfFacilities));
        logger.info(String.format("Number of adjusted facilities: %d (%.2f%%)", numberOfAdjustedFacilities, (double)numberOfAdjustedFacilities / (double) numberOfFacilities));
    }

    private void adjustPopulation() {
        logger.info("Updating population ...");

        Counter counter = new Counter("", " plans processed");

        long numberOfPlans = 0;
        long numberOfAdjustedPlans = 0;

        long numberOfActivities = 0;
        long numberOfAdjustedActivities = 0;

        long numberOfLegs = 0;
        long numberOfAdjustedLegs = 0;

        for (Person person : scenario.getPopulation().getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                boolean isPlanAdjusted = false;

                for (PlanElement planElement : plan.getPlanElements()) {
                    if (planElement instanceof Activity) {
                        Activity activity = (Activity) planElement;
                        numberOfActivities++;

                        if (linkReplacements.containsKey(activity.getLinkId())) {
                            activity.setLinkId(linkReplacements.get(activity.getLinkId()));
                            //activity.setCoord(scenario.getNetwork().getLinks().get(activity.getLinkId()).getCoord());
                            numberOfAdjustedActivities++;
                            isPlanAdjusted = true;
                        }
                    } else if (planElement instanceof Leg) {
                        numberOfLegs++;

                        Leg leg = (Leg) planElement;
                        Route route = leg.getRoute();

                        if (route instanceof NetworkRoute) {
                            adjustRoute((NetworkRoute) route);
                            numberOfAdjustedLegs++;
                            isPlanAdjusted = true;
                        }
                    }
                }

                counter.incCounter();
                numberOfPlans++;
                if (isPlanAdjusted) numberOfAdjustedPlans++;
            }
        }

        logger.info(String.format("Number of plans: %d", numberOfPlans));
        logger.info(String.format("Number of adjusted plans: %d (%.2f%%)", numberOfAdjustedPlans, (double)numberOfAdjustedPlans / (double) numberOfPlans));

        logger.info(String.format("Number of activities: %d", numberOfActivities));
        logger.info(String.format("Number of adjusted activities: %d (%.2f%%)", numberOfAdjustedActivities, (double)numberOfAdjustedActivities / (double) numberOfActivities));

        logger.info(String.format("Number of legs: %d", numberOfLegs));
        logger.info(String.format("Number of adjusted legs: %d (%.2f%%)", numberOfAdjustedLegs, (double)numberOfAdjustedLegs / (double) numberOfLegs));
    }

    private void adjustTransitSchedule() {
        logger.info("Updating transit schedule ...");
        TransitSchedule schedule = scenario.getTransitSchedule();

        long numberOfStopFacilities = 0;
        long numberOfAdjustedStopFacilities = 0;

        Counter counter = new Counter("", " stop facilities processed");

        for (TransitStopFacility stopFacility : schedule.getFacilities().values()) {
            if (linkReplacements.containsKey(stopFacility.getLinkId())) {
                stopFacility.setLinkId(linkReplacements.get(stopFacility.getLinkId()));
                //stopFacility.setCoord(scenario.getNetwork().getLinks().get(stopFacility.getLinkId()).getCoord());
                numberOfAdjustedStopFacilities++;
            }

            numberOfStopFacilities++;
            counter.incCounter();
        }

        logger.info(String.format("Number of stop facilities: %d", numberOfStopFacilities));
        logger.info(String.format("Number of adjusted stop facilities: %d (%.2f%%)", numberOfAdjustedStopFacilities, (double)numberOfAdjustedStopFacilities / (double) numberOfStopFacilities));

        long numberOfRoutes = 0;
        long numberOfAdjustedRoutes = 0;

        counter = new Counter("", " routes processed");

        for (TransitLine line : schedule.getTransitLines().values()) {
            for (TransitRoute route : line.getRoutes().values()) {
                numberOfRoutes++;
                counter.incCounter();
                if (adjustRoute(route.getRoute())) numberOfAdjustedRoutes++;
            }
        }

        logger.info(String.format("Number of transit routes: %d", numberOfRoutes));
        logger.info(String.format("Number of adjusted transit routes: %d (%.2f%%)", numberOfAdjustedRoutes, (double)numberOfAdjustedRoutes / (double) numberOfRoutes));
    }

    private boolean adjustRoute(NetworkRoute route) {
        boolean adjusted = false;

        Id<Link> startLinkId = route.getStartLinkId();
        Id<Link> endLinkId = route.getStartLinkId();
        List<Id<Link>> initialBetweenLinkIds = route.getLinkIds();
        List<Id<Link>> betweenLinkIds;

        if (linkReplacements.containsKey(startLinkId)) {
            route.setStartLinkId(linkReplacements.get(startLinkId));
            adjusted = true;
        }

        if (linkReplacements.containsKey(endLinkId)) {
            route.setEndLinkId(linkReplacements.get(endLinkId));
            adjusted = true;
        }

        betweenLinkIds = initialBetweenLinkIds.stream().filter(id -> !linkReplacements.containsKey(id)).collect(Collectors.toList());
        if (betweenLinkIds.size() != initialBetweenLinkIds.size()) adjusted = true;

        route.setLinkIds(startLinkId, betweenLinkIds, endLinkId);

        return adjusted;
    }
}


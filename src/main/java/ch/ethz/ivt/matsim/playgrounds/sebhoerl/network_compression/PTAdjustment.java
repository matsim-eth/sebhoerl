package ch.ethz.ivt.matsim.playgrounds.sebhoerl.network_compression;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.*;

import java.util.*;
import java.util.stream.Collectors;

public class PTAdjustment {
    final static private Logger logger = Logger.getLogger(PTAdjustment.class);

    static public void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

        new TransitScheduleReader(scenario).readFile(args[0]);
        new MatsimNetworkReader(scenario.getNetwork()).readFile(args[1]);

        new PTAdjustment().adjustTransitSchedule(scenario.getTransitSchedule(), scenario.getNetwork());

        new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(args[2]);
    }

    public void adjustTransitSchedule(TransitSchedule schedule, Network network) {
        Map<Id<Link>, Id<Link>> linkReplacements = new HashMap<>();

        for (Link link : network.getLinks().values()) {
            String replacements = (String) link.getAttributes().getAttribute("replaces");

            if (replacements != null) {
                for (String linkId : replacements.split(",")) {
                    linkReplacements.put(link.getId(), Id.createLinkId(linkId));
                }
            }
        }

        long numberOfStopFacilities = 0;
        long numberOfAdjustedStopFacilities = 0;

        for (TransitStopFacility stopFacility : schedule.getFacilities().values()) {
            if (linkReplacements.containsKey(stopFacility.getLinkId())) {
                stopFacility.setLinkId(linkReplacements.get(stopFacility.getLinkId()));
                numberOfAdjustedStopFacilities++;
            }

            numberOfStopFacilities++;
        }

        logger.info(String.format("Number of stop facilities: %d", numberOfStopFacilities));
        logger.info(String.format("Number of adjusted stop facilities: %d (%.2f%%)", numberOfAdjustedStopFacilities, (double)numberOfAdjustedStopFacilities / (double) numberOfStopFacilities));

        long numberOfRoutes = 0;
        long numberOfAdjustedRoutes = 0;

        for (TransitLine line : schedule.getTransitLines().values()) {
            for (TransitRoute route : line.getRoutes().values()) {
                boolean adjusted = false;

                Id<Link> startLinkId = route.getRoute().getStartLinkId();
                Id<Link> endLinkId = route.getRoute().getStartLinkId();
                List<Id<Link>> initialBetweenLinkIds = route.getRoute().getLinkIds();
                List<Id<Link>> betweenLinkIds;

                if (linkReplacements.containsKey(startLinkId)) {
                    route.getRoute().setStartLinkId(linkReplacements.get(startLinkId));
                    adjusted = true;
                }

                if (linkReplacements.containsKey(endLinkId)) {
                    route.getRoute().setEndLinkId(linkReplacements.get(endLinkId));
                    adjusted = true;
                }

                betweenLinkIds = initialBetweenLinkIds.stream().filter(id -> !linkReplacements.containsKey(id)).collect(Collectors.toList());
                if (betweenLinkIds.size() != initialBetweenLinkIds.size()) adjusted = true;

                route.getRoute().setLinkIds(startLinkId, betweenLinkIds, endLinkId);

                numberOfRoutes++;
                if (adjusted) numberOfAdjustedRoutes++;
            }
        }

        logger.info(String.format("Number of transit routes: %d", numberOfRoutes));
        logger.info(String.format("Number of adjusted transit routes: %d (%.2f%%)", numberOfAdjustedRoutes, (double)numberOfAdjustedRoutes / (double) numberOfRoutes));
    }
}


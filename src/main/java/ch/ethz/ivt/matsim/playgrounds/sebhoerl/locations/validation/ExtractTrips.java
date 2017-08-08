package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.validation;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.DebugInformation;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.DebugInformationRoute;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.DebugInformationRouteFactory;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.MatsimFacilitiesReader;
import org.matsim.pt.PtConstants;

import java.io.*;
import java.util.List;

public class ExtractTrips {
    static public void main(String[] args) throws IOException {
        String populationInputPath = args[0];
        String facilitiesInputPath = args[1];
        String outputPath = args[2];

        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(DebugInformationRoute.class, new DebugInformationRouteFactory());

        new PopulationReader(scenario).readFile(populationInputPath);
        new MatsimFacilitiesReader(scenario).readFile(facilitiesInputPath);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));

        for (Person person : scenario.getPopulation().getPersons().values()) {
            Plan plan = person.getSelectedPlan();
            List<TripStructureUtils.Trip> trips = TripStructureUtils.getTrips(plan.getPlanElements(), new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE));

            for (TripStructureUtils.Trip trip : trips) {
                Coord originCoord = trip.getOriginActivity().getCoord();
                Coord destinationCoord = trip.getDestinationActivity().getCoord();

                ActivityFacility originFacility = scenario.getActivityFacilities().getFacilities().get(trip.getOriginActivity().getFacilityId());
                ActivityFacility destinationFacility = scenario.getActivityFacilities().getFacilities().get(trip.getDestinationActivity().getFacilityId());

                if (CoordUtils.calcEuclideanDistance(originCoord, originFacility.getCoord()) > 1.0) {
                    throw new RuntimeException();
                }

                if (CoordUtils.calcEuclideanDistance(destinationCoord, destinationFacility.getCoord()) > 1.0) {
                    throw new RuntimeException();
                }

                if (trip.getLegsOnly().size() > 1) {
                    throw new RuntimeException();
                }

                Leg leg = trip.getLegsOnly().get(0);
                DebugInformation debugInformation = (leg.getRoute() instanceof DebugInformationRoute) ? ((DebugInformationRoute) leg.getRoute()).getDebugInformation() : null;

                String data = String.format("%f %f %f %f %f %s %s %b %f %b %b %d %s %f %f",
                        originCoord.getX(), originCoord.getY(),
                        destinationCoord.getX(), destinationCoord.getY(),
                        trip.getDestinationActivity().getStartTime() - trip.getOriginActivity().getEndTime(),
                        leg.getMode(),
                        trip.getDestinationActivity().getType(),
                        debugInformation != null,
                        debugInformation != null ? debugInformation.discretizationError : 0.0,
                        debugInformation != null ? debugInformation.isDistanceChainSampledConverged : false,
                        debugInformation != null ? debugInformation.isDiscreteChainSolverConverged : false,
                        debugInformation != null ? debugInformation.lengthOfChain : 0,
                        person.getId().toString(),
                        debugInformation != null ? debugInformation.discretizedDistance : 0.0,
                        debugInformation != null ? debugInformation.referenceDistance : 0.0
                );

                writer.write(data + "\n");
                writer.flush();
            }
        }

        writer.close();
    }
}

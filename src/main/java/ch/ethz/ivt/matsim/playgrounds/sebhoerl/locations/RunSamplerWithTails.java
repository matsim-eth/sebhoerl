package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.discretizer.LocationDiscretizer;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver.DiscreteChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver.IterativeDiscreteChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver.IterativeDiscreteTailSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.*;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.discrete.FacilityDiscretizer;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.discrete.FacilityDiscretizerFactory;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.discrete.FacilityLocation;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.microcensus.Microcensus;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.microcensus.MicrocensusReader;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.ContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.GravityContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.AngularContinuousTailSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.trip_chain.TripChain;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.trip_chain.TripChainFinder;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner.BasicInputChain;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner.BasicInputChainElement;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner.LocationAssignment;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.*;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances.DistanceDistribution;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.MatsimFacilitiesReader;
import org.matsim.pt.PtConstants;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class RunSamplerWithTails {
    static public void main(String[] args) throws IOException {
        Microcensus microcensus = new Microcensus(); //new Microcensus(300, 30.0 * 3600.0, 100);
        MicrocensusReader loader = new MicrocensusReader(microcensus);
        loader.read(new File(args[0]));

        // Load MATSim stuff
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile(args[1]);
        new MatsimFacilitiesReader(scenario).readFile(args[2]);

        for (Person person : scenario.getPopulation().getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                for (PlanElement element : plan.getPlanElements()) {
                    if (element instanceof Activity) {
                        Activity activity = (Activity) element;

                        if (activity.getType().equals("shopping")) {
                            activity.setType("shop");
                        }

                        activity.setType(activity.getType().replaceAll("_[0-9]+$", ""));
                    }
                }
            }
        }

        Iterator<? extends Person> personIterator = scenario.getPopulation().getPersons().values().iterator();

        while (personIterator.hasNext()) {
            Person person = personIterator.next();
            List<Activity> activites = person.getSelectedPlan().getPlanElements().stream().filter(Activity.class::isInstance).map(Activity.class::cast).collect(Collectors.toList());
            //if (!activites.get(0).getType().equals("home") || !activites.get(activites.size() - 1).getType().equals("home")) personIterator.remove();
            if (!activites.get(0).getType().equals("home")) personIterator.remove();
        }

        Map<String, FacilityDiscretizer> discretizers = new FacilityDiscretizerFactory(new Random(0L), 50.0)
                .createDiscretizers(scenario.getActivityFacilities().getFacilities().values());

        Set<String> activityTypes = new HashSet<>(Arrays.asList("shop", "leisure", "escort_kids", "escort_other", "remote_work", "remote_home"));
        StageActivityTypes stageActivityTypes = new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE);
        TripChainFinder tripChainFinder = new TripChainFinder(activityTypes, stageActivityTypes);

        // Prepare Location Choice

        ContinuousChainSolver chainSolver = new GravityContinuousChainSolver(new Random(0), 0.1, 10.0, 1000);
        ChainSampler chainSampler = new ChainSampler(chainSolver, 1000);
        DiscreteChainSolver discreteChainSolver = new IterativeDiscreteChainSolver(chainSolver, 1000);

        AngularContinuousTailSolver tailSolver = new AngularContinuousTailSolver(new Random(0));
        TailSampler tailSampler = new TailSampler(tailSolver, 1000);
        IterativeDiscreteTailSolver discreteTailSolver = new IterativeDiscreteTailSolver(tailSolver, 1000);

        LocationAssignment locationAssignment = new LocationAssignment(chainSampler, tailSampler, discreteChainSolver, discreteTailSolver);

        DescriptiveStatistics statistics = new DescriptiveStatistics();
        Map<String, DescriptiveStatistics> statisticsByMode = new HashMap<>();

        for (String mode : new String[] { "car", "pt", "bike", "walk" }) {
            statisticsByMode.put(mode, new DescriptiveStatistics());
        }

        // Perform discrete choice

        Set<Id<Person>> failedIds = new HashSet<>();
        Set<Id<Person>> invalidStaticIds = new HashSet<>();

        long numberOfPersons = 0;
        Random random = new Random(0L);

        for (Person person : scenario.getPopulation().getPersons().values()) {
            //if (numberOfPersons == 500) break;
            List<TripChain> tripChains = tripChainFinder.findTripChains(person.getSelectedPlan());

            for (TripChain tripChain : tripChains) {
                // Preparation of trip chain

                BasicInputChain inputChain = new BasicInputChain(tripChain.isVariableBeginning(), tripChain.isVariableEnd());

                for (TripStructureUtils.Trip trip : tripChain.getTrips()) {
                    double travelTime = Math.max(0.0, trip.getDestinationActivity().getStartTime() - trip.getOriginActivity().getEndTime());

                    LocationDiscretizer locationDiscretizer = discretizers.get(trip.getDestinationActivity().getType());
                    DistanceDistribution distanceDistribution = null;
                    double discretizationThreshold;

                    Coord originCoord = trip.getOriginActivity().getCoord();
                    Coord destinationCoord = trip.getDestinationActivity().getCoord();

                    Vector2D originLocation = new Vector2D(originCoord.getX(), originCoord.getY());
                    Vector2D destinationLocation = new Vector2D(destinationCoord.getX(), destinationCoord.getY());

                    switch (trip.getLegsOnly().get(0).getMode()) {
                        case "car":
                            distanceDistribution = microcensus.createDistanceDistribution(random, Microcensus.Mode.car, travelTime);
                            discretizationThreshold = 1000.0;
                            break;
                        case "pt":
                            distanceDistribution = microcensus.createDistanceDistribution(random, Microcensus.Mode.pt, travelTime);
                            discretizationThreshold = 1000.0;
                            break;
                        case "bike":
                            distanceDistribution = microcensus.createDistanceDistribution(random, Microcensus.Mode.bike, travelTime);
                            discretizationThreshold = 200.0;
                            break;
                        case "walk":
                            distanceDistribution = microcensus.createDistanceDistribution(random, Microcensus.Mode.walk, travelTime);
                            discretizationThreshold = 50.0;
                            break;
                        default:
                            throw new RuntimeException();
                    }

                    inputChain.getElements().add(new BasicInputChainElement(originLocation, destinationLocation, distanceDistribution, locationDiscretizer, discretizationThreshold));
                }

                // Perform location assignment

                LocationAssignment.Result locationAssignmentResult = locationAssignment.processInputChain(inputChain);

                // Transfer back to MATSim

                List<Activity> activities = tripChain.getActivities();

                for (int i = 0; i < activities.size(); i++) {
                    Activity activity = activities.get(i);
                    FacilityLocation location = (FacilityLocation) locationAssignmentResult.discreteLocations.get(i);

                    if (location != null) {
                        activity.setFacilityId(location.getFacility().getId());
                        activity.setLinkId(location.getFacility().getLinkId());
                        activity.setCoord(location.getFacility().getCoord());
                    }
                }

                // Collect debug information into legs

                for (int i = 0; i < tripChain.getTrips().size(); i++) {
                    TripStructureUtils.Trip trip = tripChain.getTrips().get(i);
                    Leg leg = trip.getLegsOnly().get(0);

                    double travelTime = trip.getDestinationActivity().getStartTime() - trip.getOriginActivity().getEndTime();
                    DebugInformation debugInformation = new DebugInformation(i, tripChain.getTrips().size(), locationAssignmentResult.chainSamplerResult, locationAssignmentResult.discreteSolverResult);

                    leg.setRoute(new DebugInformationRoute(travelTime, debugInformation));
                }

                if (locationAssignmentResult.chainSamplerResult.isConverged()) {
                    for (int i = 0; i < tripChain.getTrips().size(); i++) {
                        double difference = Math.abs(locationAssignmentResult.discreteSolverResult.getDiscreteDistances().get(i) - locationAssignmentResult.chainSamplerResult.getContinuousSolverResult().getDistances().get(i));
                        statistics.addValue(difference);
                        statisticsByMode.get(tripChain.getTrips().get(i).getLegsOnly().get(0).getMode()).addValue(difference);
                    }
                } else if (tripChain.getTrips().size() > 1) {
                    failedIds.add(person.getId());
                } else {
                    invalidStaticIds.add(person.getId());
                }
            }

            numberOfPersons++;
            System.out.println(String.format("Total: %d, Failed: %d (%.2f%%)", numberOfPersons, failedIds.size(), 100.0 * failedIds.size() / numberOfPersons));

            System.out.println(String.format("   Median discretization error: %.2f", statistics.getPercentile(50.0)));
            for (String mode : new String[] { "car", "pt", "bike", "walk" }) {
                System.out.println(String.format("   Median discretization error (%s): %.2f", mode, statisticsByMode.get(mode).getPercentile(50.0)));
            }

            System.out.println();
        }

        new PopulationWriter(scenario.getPopulation()).write(args[3]);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[4])));
        for (Id<Person> personId : failedIds) writer.write(personId.toString() + "\n");
        writer.flush();
        writer.close();
    }
}

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.DiscreteLocation;
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

public class RunSampler {
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
            if (!activites.get(0).getType().equals("home") || !activites.get(activites.size() - 1).getType().equals("home")) personIterator.remove();
        }

        Map<String, FacilityDiscretizer> discretizers = new FacilityDiscretizerFactory(new Random(0L), 50.0)
                .createDiscretizers(scenario.getActivityFacilities().getFacilities().values());

        Set<String> activityTypes = new HashSet<>(Arrays.asList("shop", "leisure", "escort_kids", "escort_other", "remote_work", "remote_home"));
        StageActivityTypes stageActivityTypes = new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE);
        TripChainFinder tripChainFinder = new TripChainFinder(activityTypes, stageActivityTypes);

        // Prepare Location Choice

        ContinuousChainSolver chainSolver = new GravityContinuousChainSolver(new Random(0), 0.1, 10.0, 1000);
        ChainSampler sampler = new ChainSampler(chainSolver, 1000);
        DiscreteChainSolver discreteSolver = new IterativeDiscreteChainSolver(chainSolver, 1000);

        AngularContinuousTailSolver tailGenerator = new AngularContinuousTailSolver(new Random(0));
        TailSampler tailChainSampler = new TailSampler(tailGenerator, 1000);
        IterativeDiscreteTailSolver discreteTailSolver = new IterativeDiscreteTailSolver(tailGenerator, 1000);

        DescriptiveStatistics statistics = new DescriptiveStatistics();
        Map<String, DescriptiveStatistics> statisticsByMode = new HashMap<>();

        for (String mode : new String[] { "car", "pt", "bike", "walk" }) {
            statisticsByMode.put(mode, new DescriptiveStatistics());
        }

        // Perform discrete choice

        Set<Id<Person>> failedIds = new HashSet<>();
        Set<Id<Person>> invalidStaticIds = new HashSet<>();

        long numberOfPersons = 0;
        long totalNumberOfPersons = scenario.getPopulation().getPersons().size();
        Random random = new Random(0L);

        for (Person person : scenario.getPopulation().getPersons().values()) {
            //if (numberOfPersons > 500) break;

            List<TripChain> tripChains = tripChainFinder.findTripChains(person.getSelectedPlan());

            for (TripChain tripChain : tripChains) {
                // Preparation of trip chain

                List<DistanceDistribution> distributions = new LinkedList<>();
                List<Double> thresholds = new LinkedList<>();

                for (TripStructureUtils.Trip trip : tripChain.getTrips()) {
                    double travelTime = Math.max(0.0, trip.getDestinationActivity().getStartTime() - trip.getOriginActivity().getEndTime());

                    switch (trip.getLegsOnly().get(0).getMode()) {
                        case "car":
                            distributions.add(microcensus.createDistanceDistribution(random, Microcensus.Mode.car, travelTime));
                            //thresholds.add(1000.0);
                            thresholds.add(100.0);
                            break;
                        case "pt":
                            distributions.add(microcensus.createDistanceDistribution(random, Microcensus.Mode.pt, travelTime));
                            //thresholds.add(1000.0);
                            thresholds.add(100.0);
                            break;
                        case "bike":
                            distributions.add(microcensus.createDistanceDistribution(random, Microcensus.Mode.bike, travelTime));
                            //thresholds.add(200.0);
                            thresholds.add(20.0);
                            break;
                        case "walk":
                            distributions.add(microcensus.createDistanceDistribution(random, Microcensus.Mode.walk, travelTime));
                            //thresholds.add(50.0);
                            thresholds.add(5.0);
                            break;
                        default:
                            throw new RuntimeException();
                    }
                }

                List<LocationDiscretizer> tripDiscretizers = new LinkedList<>();
                for (TripStructureUtils.Trip trip : tripChain.getTrips()) {
                    tripDiscretizers.add(discretizers.get(trip.getDestinationActivity().getType()));
                }

                Coord originCoord = tripChain.getOriginActivity().getCoord();
                Coord destinationCoord = tripChain.getDestinatonActivity().getCoord();

                Vector2D originLocation = new Vector2D(originCoord.getX(), originCoord.getY());
                Vector2D destinationLocation = new Vector2D(destinationCoord.getX(), destinationCoord.getY());

                ChainSamplerResult result = sampler.sample(originLocation, destinationLocation, distributions);
                ContinuousChainSolver.Result chainResult = result.getContinuousSolverResult();

                DiscreteChainSolver.Result discreteResult = discreteSolver.solve(originLocation, destinationLocation, chainResult.getDistances(), thresholds, tripDiscretizers);

                for (int i = 0; i < tripChain.getTrips().size(); i++) {
                    TripStructureUtils.Trip trip = tripChain.getTrips().get(i);
                    Activity destinationActivity = trip.getDestinationActivity();
                    Leg leg = trip.getLegsOnly().get(0);

                    if (i < tripChain.getTrips().size() - 1) {
                        DiscreteLocation discreteLocation = discreteResult.getDiscreteLocations().get(i);
                        FacilityLocation facilityLocation = (FacilityLocation) discreteLocation;
                        destinationActivity.setCoord(new Coord(discreteLocation.getLocation().getX(), discreteLocation.getLocation().getY()));
                        destinationActivity.setFacilityId(facilityLocation.getFacility().getId());
                    }

                    double travelTime = trip.getDestinationActivity().getStartTime() - trip.getOriginActivity().getEndTime();
                    DebugInformation debugInformation = new DebugInformation(i, tripChain.getTrips().size(), result, discreteResult);
                    leg.setRoute(new DebugInformationRoute(travelTime, debugInformation));
                }

                if (result.isConverged()) {
                    for (int i = 0; i < tripChain.getTrips().size(); i++) {
                        double difference = Math.abs(discreteResult.getDiscreteDistances().get(i) - chainResult.getDistances().get(i));
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
            System.out.println(String.format("Total: %d (%.2f%%), Failed: %d (%.2f%%)", numberOfPersons, (double) numberOfPersons / (double) totalNumberOfPersons, failedIds.size(), 100.0 * failedIds.size() / numberOfPersons));

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

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.*;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.*;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.microcensus.Microcensus;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.microcensus.MicrocensusReader;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation.ContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation.GravityChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation.TailGenerator;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.*;
import org.apache.commons.math3.distribution.GammaDistribution;
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
        Microcensus microcensus = new Microcensus(300, 30.0 * 3600.0, 100);
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

        Map<String, FacilityDiscretizer> discretizers = new FacilityDiscretizerFactory(new Random(0L), 50.0)
                .createDiscretizers(scenario.getActivityFacilities().getFacilities().values());

        SpeedDistributionAdapter carDistribution = new SpeedDistributionAdapter(new GammaDistribution(2.2603590129 , 8.42709579365));
        SpeedDistributionAdapter ptDistribution = new SpeedDistributionAdapter(new GammaDistribution(1.9244814414 , 4.76363476292));
        SpeedDistributionAdapter bikeDistribution = new SpeedDistributionAdapter(new GammaDistribution(2.49921425903 , 3.24505854558));
        SpeedDistributionAdapter walkDistribution = new SpeedDistributionAdapter(new GammaDistribution(1.92458921655 , 1.6967512488));

        Set<String> activityTypes = new HashSet<>(Arrays.asList("shop", "leisure", "escort_kids", "escort_other", "remote_work", "remote_home"));
        StageActivityTypes stageActivityTypes = new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE);
        TripChainFinder tripChainFinder = new TripChainFinder(activityTypes, stageActivityTypes);

        // Prepare Location Choice

        ContinuousChainSolver chainSolver = new GravityChainSolver(new Random(0), 0.1, 10.0, 1000);
        DistanceChainSampler sampler = new DistanceChainSampler(chainSolver, 1000);
        DiscreteChainSolver discreteSolver = new SamplingChainSolver(chainSolver, 1000);

        TailGenerator tailGenerator = new TailGenerator(new Random(0));
        TailChainSampler tailChainSampler = new TailChainSampler(tailGenerator);
        SamplingTailSolver discreteTailSolver = new SamplingTailSolver(tailGenerator, 1000);

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
                List<DistanceDistribution> distributions = new LinkedList<>();
                List<Double> thresholds = new LinkedList<>();

                for (TripStructureUtils.Trip trip : tripChain.getTrips()) {
                    double travelTime = Math.max(0.0, trip.getDestinationActivity().getStartTime() - trip.getOriginActivity().getEndTime());

                    switch (trip.getLegsOnly().get(0).getMode()) {
                        case "car":
                            distributions.add(microcensus.createDistanceDistribution(random, Microcensus.Mode.car, travelTime));
                            thresholds.add(1000.0);
                            //distributions.add(carDistribution.createDistribution(travelTime));
                            break;
                        case "pt":
                            distributions.add(microcensus.createDistanceDistribution(random, Microcensus.Mode.pt, travelTime));
                            thresholds.add(1000.0);
                            //distributions.add(ptDistribution.createDistribution(travelTime));
                            break;
                        case "bike":
                            distributions.add(microcensus.createDistanceDistribution(random, Microcensus.Mode.bike, travelTime));
                            thresholds.add(200.0);
                            //distributions.add(bikeDistribution.createDistribution(travelTime));
                            break;
                        case "walk":
                            distributions.add(microcensus.createDistanceDistribution(random, Microcensus.Mode.walk, travelTime));
                            thresholds.add(50.0);
                            //distributions.add(walkDistribution.createDistribution(travelTime));
                            break;
                        default:
                            throw new RuntimeException();
                    }
                }

                Coord originCoord = tripChain.getOriginActivity().getCoord();
                Coord destinationCoord = tripChain.getDestinatonActivity().getCoord();

                Vector2D originLocation = new Vector2D(originCoord.getX(), originCoord.getY());
                Vector2D destinationLocation = new Vector2D(destinationCoord.getX(), destinationCoord.getY());

                DistanceChainSampler.Result result = sampler.sample(originLocation, destinationLocation, distributions);
                ContinuousChainSolver.Result chainResult = result.getChainResult();

                List<LocationDiscretizer> tripDiscretizers = new LinkedList<>();
                for (TripStructureUtils.Trip trip : tripChain.getTrips()) {
                    tripDiscretizers.add(discretizers.get(trip.getDestinationActivity().getType()));
                }

                DiscreteChainSolver.Result discreteResult = discreteSolver.findDiscreteLocations(originLocation, destinationLocation, chainResult.getDistances(), thresholds, tripDiscretizers);

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

                    leg.setRoute(new DebugRoute(
                            travelTime,
                            i,
                            result,
                            discreteResult
                    ));
                }

                if (result.isValid()) {
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

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.*;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation.ContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation.GravityChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.DistanceChainSampler;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.DistanceDistribution;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.TruncatedNormalDistanceDistribution;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.*;
import java.util.stream.Collectors;

public class RunExample {
    static public void main(String[] args) {
        // 1. Create a grid of discrete locations

        Collection<DiscreteLocation> locations = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                locations.add(new BasicDiscreteLocation(new Vector2D(10.0 * i, 10.0 * j)));
            }
        }

        // 2. Create a chain
        int numberOfElements = 4;

        Vector2D initialLocation = new Vector2D(10.0, 10.0);
        Vector2D finalLocation = new Vector2D(80.0, 60.0);

        // 3. Set up
        ContinuousChainSolver chainSolver = new GravityChainSolver(new Random(0L), 0.1, 1e-3, 1000);
        DistanceDistribution distanceDistribution = new TruncatedNormalDistanceDistribution(20.0, 10.0);

        DistanceChainSampler sampler = new DistanceChainSampler(chainSolver, 1000);
        DistanceChainSampler.Result result = sampler.sample(initialLocation, finalLocation, Collections.nCopies(numberOfElements, distanceDistribution));

        if (!result.isValid()) throw new RuntimeException();

        List<Vector2D> allLocations = new LinkedList<>(result.getChainResult().getLocations());
        allLocations.add(0, initialLocation);
        allLocations.add(finalLocation);

        SetLocationDiscretizer discretizer = new SetLocationDiscretizer(locations);
        DiscreteChainSolver discreteSolver = new SamplingChainSolver(chainSolver, 1000);

        List<Double> distances = new LinkedList<>();
        for (int i = 0; i < numberOfElements; i++) distances.add(allLocations.get(i).distance(allLocations.get(i + 1)));

        DiscreteChainSolver.Result discreteResult = discreteSolver.findDiscreteLocations(initialLocation, finalLocation, distances, Collections.nCopies(numberOfElements, 5.0), Collections.nCopies(numberOfElements - 1, discretizer));
        List<DiscreteLocation> discreteLocations = discreteResult.getDiscreteLocations();

        for (int i = 0; i < discreteLocations.size(); i++) {
            System.out.println(discreteLocations.get(i).getLocation());
        }
    }
}

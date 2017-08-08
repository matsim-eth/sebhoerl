package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.ContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.GravityContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.BasicDiscreteLocation;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.DiscreteLocation;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.discretizer.LocationSetDiscretizer;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver.DiscreteChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver.IterativeDiscreteChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.ChainSampler;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.ChainSamplerResult;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances.DistanceDistribution;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances.TruncatedNormalDistanceDistribution;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.*;

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
        ContinuousChainSolver chainSolver = new GravityContinuousChainSolver(new Random(0L), 0.1, 1e-3, 1000);
        DistanceDistribution distanceDistribution = new TruncatedNormalDistanceDistribution(20.0, 10.0);

        ChainSampler sampler = new ChainSampler(chainSolver, 1000);
        ChainSamplerResult result = sampler.sample(initialLocation, finalLocation, Collections.nCopies(numberOfElements, distanceDistribution));

        if (!result.isConverged()) throw new RuntimeException();

        List<Vector2D> allLocations = new LinkedList<>(result.getContinuousSolverResult().getLocations());
        allLocations.add(0, initialLocation);
        allLocations.add(finalLocation);

        LocationSetDiscretizer discretizer = new LocationSetDiscretizer(locations);
        DiscreteChainSolver discreteSolver = new IterativeDiscreteChainSolver(chainSolver, 1000);

        List<Double> distances = new LinkedList<>();
        for (int i = 0; i < numberOfElements; i++) distances.add(allLocations.get(i).distance(allLocations.get(i + 1)));

        DiscreteChainSolver.Result discreteResult = discreteSolver.solve(initialLocation, finalLocation, distances, Collections.nCopies(numberOfElements, 5.0), Collections.nCopies(numberOfElements - 1, discretizer));
        List<DiscreteLocation> discreteLocations = discreteResult.getDiscreteLocations();

        for (int i = 0; i < discreteLocations.size(); i++) {
            System.out.println(discreteLocations.get(i).getLocation());
        }
    }
}

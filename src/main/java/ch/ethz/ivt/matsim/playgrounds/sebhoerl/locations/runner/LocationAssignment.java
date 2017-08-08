package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous.ReversedTailSolverResult;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.DiscreteLocation;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.discretizer.LocationDiscretizer;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver.DiscreteChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver.DiscreteSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver.DiscreteTailSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver.ReversedDiscreteTailSolverResult;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.ChainSampler;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.ChainSamplerResult;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.TailSampler;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances.DistanceDistribution;
import com.google.common.collect.Lists;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class LocationAssignment {
    final private ChainSampler chainSampler;
    final private TailSampler tailSampler;
    final private DiscreteChainSolver discreteChainSolver;
    final private DiscreteTailSolver discreteTailSolver;

    public LocationAssignment(ChainSampler chainSampler, TailSampler tailSampler, DiscreteChainSolver discreteChainSolver, DiscreteTailSolver discreteTailSolver) {
        this.chainSampler = chainSampler;
        this.tailSampler = tailSampler;
        this.discreteChainSolver = discreteChainSolver;
        this.discreteTailSolver = discreteTailSolver;
    }

    public class Result {
        public List<DiscreteLocation> discreteLocations;
        public List<Vector2D> sampledLocations;

        public List<Double> discretizedDistances;
        public List<Double> sampledDistances;

        public ChainSamplerResult chainSamplerResult;
        public DiscreteSolver.Result discreteSolverResult;
    }

    public Result processInputChain(InputChain inputChain) {
        List<DistanceDistribution> distributions = inputChain.getElements().stream().map(e -> e.getDistanceDistribution()).collect(Collectors.toList());
        List<Double> discretizationThresholds = inputChain.getElements().stream().map(e -> e.getDiscretizationThreshold()).collect(Collectors.toList());
        List<LocationDiscretizer> locationDiscretizers = inputChain.getElements().stream().map(e -> e.getLocationDiscretizer()).collect(Collectors.toList());

        Vector2D originLocation = inputChain.getElements().get(0).getOriginLocation();
        Vector2D destinationLocation = inputChain.getElements().get(inputChain.getElements().size() - 1).getDestinationLocation();

        if (inputChain.hasVariableOriginLocation()) {
            return processTail(false, destinationLocation, distributions, discretizationThresholds, locationDiscretizers);
        } else if (inputChain.hasVariableDestinationLocation()) {
            return processTail(true, originLocation, distributions, discretizationThresholds, locationDiscretizers);
        } else if (!inputChain.hasVariableOriginLocation() && !inputChain.hasVariableDestinationLocation()) {
            return processChain(originLocation, destinationLocation, distributions, discretizationThresholds, locationDiscretizers);
        } else {
            throw new IllegalArgumentException("Completely variable input chains are not supported!");
        }
    }

    public Result processChain(Vector2D originLocation, Vector2D destinationLocation, List<DistanceDistribution> distanceDistributions, List<Double> discretizationThresholds, List<LocationDiscretizer> locationDiscretizers) {
        ChainSamplerResult chainSamplerResult = chainSampler.sample(originLocation, destinationLocation, distanceDistributions);
        DiscreteChainSolver.Result discreteResult = discreteChainSolver.solve(
                originLocation, destinationLocation,
                chainSamplerResult.getContinuousSolverResult().getDistances(),
                discretizationThresholds, locationDiscretizers);

        Result result = new Result();
        result.sampledDistances = chainSamplerResult.getContinuousSolverResult().getDistances();
        result.discretizedDistances = discreteResult.getDiscreteDistances();

        LinkedList<Vector2D> sampledLocations = new LinkedList<>(chainSamplerResult.getContinuousSolverResult().getLocations());
        sampledLocations.add(0, originLocation);
        sampledLocations.add(destinationLocation);
        result.sampledLocations = sampledLocations;

        LinkedList<DiscreteLocation> discreteLocations = new LinkedList<>(discreteResult.getDiscreteLocations());
        discreteLocations.add(0, null);
        discreteLocations.add(null);
        result.discreteLocations = discreteLocations;

        result.chainSamplerResult = chainSamplerResult;
        result.discreteSolverResult = discreteResult;

        return result;
    }

    public Result processTail(boolean isRightTail, Vector2D anchorLocation, List<DistanceDistribution> distanceDistributions, List<Double> discretizationThresholds, List<LocationDiscretizer> locationDiscretizers) {
        if (!isRightTail) {
            distanceDistributions = Lists.reverse(distanceDistributions);
            discretizationThresholds = Lists.reverse(discretizationThresholds);
            locationDiscretizers = Lists.reverse(locationDiscretizers);
        }

        ChainSamplerResult chainSamplerResult = tailSampler.sample(anchorLocation, distanceDistributions);
        DiscreteChainSolver.Result discreteResult = discreteTailSolver.solve(
                anchorLocation,
                chainSamplerResult.getContinuousSolverResult().getDistances(),
                discretizationThresholds, locationDiscretizers);

        Result result = new Result();

        if (isRightTail) {
            result.sampledDistances = chainSamplerResult.getContinuousSolverResult().getDistances();
            result.discretizedDistances = discreteResult.getDiscreteDistances();

            LinkedList<Vector2D> sampledLocations = new LinkedList<>(chainSamplerResult.getContinuousSolverResult().getLocations());
            sampledLocations.add(0, anchorLocation);
            result.sampledLocations = sampledLocations;

            LinkedList<DiscreteLocation> discreteLocations = new LinkedList<>(discreteResult.getDiscreteLocations());
            discreteLocations.add(0, null);
            result.discreteLocations = discreteLocations;

            result.chainSamplerResult = chainSamplerResult;
            result.discreteSolverResult = discreteResult;
        } else {
            result.sampledDistances = Lists.reverse(chainSamplerResult.getContinuousSolverResult().getDistances());
            result.discretizedDistances = Lists.reverse(discreteResult.getDiscreteDistances());

            LinkedList<Vector2D> sampledLocations = new LinkedList<>(Lists.reverse(chainSamplerResult.getContinuousSolverResult().getLocations()));
            sampledLocations.add(anchorLocation);
            result.sampledLocations = sampledLocations;

            LinkedList<DiscreteLocation> discreteLocations = new LinkedList<>(Lists.reverse(discreteResult.getDiscreteLocations()));
            discreteLocations.add(null);
            result.discreteLocations = discreteLocations;

            result.chainSamplerResult = new ChainSamplerResult(
                    chainSamplerResult.isConverged(),
                    new ReversedTailSolverResult(chainSamplerResult.getContinuousSolverResult()),
                    chainSamplerResult.getNumberOfSamples(),
                    chainSamplerResult.getNumberOfSolverIterations()
            );

            result.discreteSolverResult = new ReversedDiscreteTailSolverResult(discreteResult);
        }

        return result;
    }
}

































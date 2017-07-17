package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.DiscreteChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation.ContinuousChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.DistanceChainSampler;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.population.routes.AbstractRoute;
import org.matsim.core.population.routes.GenericRouteImpl;
import org.matsim.core.utils.misc.Time;

public class DebugRoute extends AbstractRoute {
    final private DistanceChainSampler.Result samplerResult;
    final private DiscreteChainSolver.Result discreteResult;

    final private int index;

    public DebugRoute(double travelTime, int index, DistanceChainSampler.Result samplerResult, DiscreteChainSolver.Result discreteResult) {
        super(Id.createLinkId(""), Id.createLinkId(""));

        this.index = index;

        this.setTravelTime(travelTime);
        this.setDistance(discreteResult.getDiscreteDistances().get(index));

        this.samplerResult = samplerResult;
        this.discreteResult = discreteResult;
    }

    @Override
    public String getRouteDescription() {
        return "\n" +
                "Index in chain: " + index + "\n" +
                "Distance chain sampler converged: " + (samplerResult.isValid() ? "yes" : "no") + "\n" +
                "   Number of samples: " + samplerResult.getSamplingIterations() + "\n" +
                "   Total number of solver iterations: " + samplerResult.getTotalSolverIterations() + "\n" +
                "Discrete chain solver converged: " + (discreteResult.isConverged() ? "yes" : "no") + "\n" +
                "   Iterations: " + discreteResult.getIterations() + "\n" +
                "   Discretization error: " + String.format("%.2f km", 1e-3 * discreteResult.getDiscreteDistances().get(index) - 1e-3 * samplerResult.getChainResult().getDistances().get(index)) + "\n"
                ;
     }

    @Override
    public void setRouteDescription(String s) {}

    @Override
    public String getRouteType() {
        return "Debug";
    }
}

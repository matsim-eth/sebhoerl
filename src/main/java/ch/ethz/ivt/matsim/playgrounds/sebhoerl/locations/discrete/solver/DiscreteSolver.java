package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.DiscreteLocation;

import java.util.List;

public interface DiscreteSolver {
    interface Result {
        List<DiscreteLocation> getDiscreteLocations();
        List<Double> getDiscreteDistances();
        long getNumberOfIterations();
        boolean isConverged();
    }
}

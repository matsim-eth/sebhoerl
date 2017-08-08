package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.discretizer.LocationDiscretizer;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;

public interface DiscreteChainSolver extends DiscreteSolver {
    Result solve(Vector2D originLocation, Vector2D destinationLocation, List<Double> distances, List<Double> convergenceThresholds, List<LocationDiscretizer> discretizers);
}

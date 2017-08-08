package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.discretizer.LocationDiscretizer;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;

public interface DiscreteTailSolver extends DiscreteSolver {
    Result solve(Vector2D anchorLocation, List<Double> distances, List<Double> convergenceThresholds, List<LocationDiscretizer> discretizers);
}

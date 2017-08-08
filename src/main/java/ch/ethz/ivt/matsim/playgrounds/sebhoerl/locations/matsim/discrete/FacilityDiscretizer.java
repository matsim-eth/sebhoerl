package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.discrete;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.DiscreteLocation;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.discretizer.LocationDiscretizer;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.matsim.core.utils.collections.QuadTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class FacilityDiscretizer implements LocationDiscretizer {
    final protected QuadTree<FacilityLocation> quadtree;
    final private double radius;
    final private Random random;

    public FacilityDiscretizer(Random random, double radius, Collection<FacilityLocation> facilities) {
        this.radius = radius;
        this.random = random;

        double minX = facilities.parallelStream().map(f -> f.getLocation().getX()).min(Double::compare).get();
        double minY = facilities.parallelStream().map(f -> f.getLocation().getY()).min(Double::compare).get();
        double maxX = facilities.parallelStream().map(f -> f.getLocation().getX()).max(Double::compare).get();
        double maxY = facilities.parallelStream().map(f -> f.getLocation().getY()).max(Double::compare).get();

        quadtree = new QuadTree(minX, minY, maxX, maxY);
        facilities.forEach(f -> quadtree.put(f.getLocation().getX(), f.getLocation().getY(), f));
    }

    @Override
    public DiscreteLocation findDiscreteLocation(Vector2D location) {
        List<FacilityLocation> candidates = new ArrayList<>(quadtree.getDisk(location.getX(), location.getY(), radius));

        if (candidates.size() > 0) {
            return candidates.get(random.nextInt(candidates.size()));
        } else {
            return quadtree.getClosest(location.getX(), location.getY());
        }
    }
}

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SetLocationDiscretizer implements LocationDiscretizer {
    final private Set<DiscreteLocation> discreteLocationSet = new HashSet<>();

    public SetLocationDiscretizer(Collection<DiscreteLocation> discreteLocations) {
        discreteLocationSet.addAll(discreteLocations);
    }

    @Override
    public DiscreteLocation findDiscreteLocation(Vector2D location) {
        List<DiscreteLocation> sortedLocations = discreteLocationSet.stream()
                .sorted((a, b) -> Double.compare(a.getLocation().distance(location), b.getLocation().distance(location)))
                .collect(Collectors.toList());

        return sortedLocations.get(0);
    }
}

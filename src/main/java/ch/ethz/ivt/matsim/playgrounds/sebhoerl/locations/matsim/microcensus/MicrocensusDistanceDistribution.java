package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.microcensus;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances.DistanceDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MicrocensusDistanceDistribution implements DistanceDistribution {
    final private List<Double> distances;
    final private Random random;

    public MicrocensusDistanceDistribution(Random random, Set<Double> distances) {
        this.random = random;
        this.distances = new ArrayList<>(distances);
    }

    @Override
    public double sample() {
        int index = random.nextInt(distances.size());
        return distances.get(index);
    }
}

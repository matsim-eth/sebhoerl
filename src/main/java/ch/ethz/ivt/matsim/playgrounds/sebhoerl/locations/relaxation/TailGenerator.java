package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TailGenerator {
    final private Random random;

    public class Result {
        final private List<Vector2D> locations;
        final private List<Double> distances;

        public Result(List<Vector2D> locations, List<Double> distances) {
            this.locations = locations;
            this.distances = distances;
        }

        public List<Vector2D> getLocations() {
            return locations;
        }

        public List<Double> getDistances() {
            return distances;
        }
    }

    public TailGenerator(Random random) {
        this.random = random;
    }

    public Result sample(Vector2D anchorLocation, List<Double> distances) {
        List<Vector2D> locations = new LinkedList<>();
        Vector2D currentLocation = anchorLocation;

        for (double distance : distances) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            Vector2D newLocation = currentLocation.add(distance, new Vector2D(Math.cos(angle), Math.sin(angle)));
            locations.add(newLocation);
            currentLocation = newLocation;
        }

        return new Result(locations, distances);
    }
}

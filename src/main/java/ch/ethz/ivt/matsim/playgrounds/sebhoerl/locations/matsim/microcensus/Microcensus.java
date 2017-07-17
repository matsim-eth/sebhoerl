package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.microcensus;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.DistanceDistribution;
import org.apache.log4j.Logger;
import org.matsim.core.utils.misc.Time;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Microcensus {
    final private static Logger logger = Logger.getLogger(Microcensus.class);

    final private long travelTimeBinSize;
    final private double maximumTravelTime;
    final private long maximumDiscretizedTravelTime;
    final private long minimumNumberOfObservationsPerDistribution;
    final private Collection<Observation> observations = new HashSet<>();

    public enum Mode {
        car, pt, bike, walk
    }

    private class Observation {
        final public double distance;
        final public double travelTime;
        final public long discretizedTravelTime;
        final public Mode mode;

        public Observation(double distance, double travelTime, long discretizedTravelTime, Mode mode) {
            this.distance = distance;
            this.travelTime = travelTime;
            this.discretizedTravelTime = discretizedTravelTime;
            this.mode = mode;
        }
    }

    public Microcensus(long travelTimeBinSize, double maximumTravelTime, long minimumNumberOfObservationsPerDistribution) {
        this.travelTimeBinSize = travelTimeBinSize;
        this.maximumTravelTime = maximumTravelTime;
        this.maximumDiscretizedTravelTime = discretizeTravelTime(maximumTravelTime);
        this.minimumNumberOfObservationsPerDistribution = minimumNumberOfObservationsPerDistribution;
    }

    private long discretizeTravelTime(double traveTime) {
        return (long) Math.floor(traveTime / (double) travelTimeBinSize);
    }

    private double recoverTravelTime(long discretizedTravelTime) {
        return discretizedTravelTime * travelTimeBinSize;
    }

    public void addObservation(double distance, double travelTime, Mode mode) {
        long discretizedTravelTime = discretizeTravelTime(travelTime);
        Observation observation = new Observation(distance, travelTime, discretizedTravelTime, mode);
        observations.add(observation);
    }

    public long getNumberOfObservations() {
        return observations.size();
    }

    final private Map<Long, DistanceDistribution> cachedDistanceDistributions = new HashMap<>();

    public DistanceDistribution createDistanceDistribution(Random random, Mode mode, double travelTime) {
        long discretizedTravelTime = discretizeTravelTime(travelTime);
        Long hash = mode.ordinal() * maximumDiscretizedTravelTime + discretizeTravelTime(travelTime);

        if (cachedDistanceDistributions.containsKey(hash)) {
            return cachedDistanceDistributions.get(hash);
        }

        long minimumDiscretizedTravelTime = discretizedTravelTime;
        long maximumDiscretizedTravelTime = discretizedTravelTime + 1;

        Stream<Observation> modeFiltered = observations.stream()
                .parallel()
                .filter(obs -> obs.mode.equals(mode));

        long numberOfAvailableObservations = modeFiltered.count();

        long numberOfFilteredObservations = 0;
        Set<Double> distances = null;

        while (numberOfFilteredObservations < Math.min(minimumNumberOfObservationsPerDistribution, numberOfAvailableObservations)) {
            final long minDtt = minimumDiscretizedTravelTime;
            final long maxDtt = maximumDiscretizedTravelTime;

            distances = observations.stream()
                    .parallel()
                    .filter(obs -> obs.mode.equals(mode))
                    .filter(obs -> obs.discretizedTravelTime >= minDtt && obs.discretizedTravelTime < maxDtt)
                    .map(obs -> obs.distance)
                    .collect(Collectors.toSet());

            numberOfFilteredObservations = distances.size();

            minimumDiscretizedTravelTime -= 1;
            maximumDiscretizedTravelTime += 1;
        }

        MicrocensusDistanceDistribution distanceDistribution = new MicrocensusDistanceDistribution(random, distances);
        cachedDistanceDistributions.put(hash, distanceDistribution);

        logger.info(String.format(
                "Create distance distribution (%s, %s): %d observations (%s - %s)",
                mode.toString(), Time.writeTime(recoverTravelTime(discretizedTravelTime)), distances.size(),
                Time.writeTime(recoverTravelTime(minimumDiscretizedTravelTime + 1)),
                Time.writeTime(recoverTravelTime(maximumDiscretizedTravelTime - 1))
                ));

        return distanceDistribution;
    }
}

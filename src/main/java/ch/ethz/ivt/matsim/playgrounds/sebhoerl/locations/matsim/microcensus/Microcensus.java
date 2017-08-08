package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.microcensus;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances.DistanceDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.matsim.core.utils.misc.Time;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Microcensus {
    final private static Logger logger = Logger.getLogger(Microcensus.class);

    final private Map<Mode, Double> travelTimeBinSize = new EnumMap<>(Mode.class);
    final private Map<Mode, Long> minimumNumberOfObservationsPerDistribution = new EnumMap<>(Mode.class);
    final private Map<Mode, Double> cutoffSpeed = new EnumMap<>(Mode.class);

    final private Collection<Observation> observations = new HashSet<>();

    public enum Mode {
        car, pt, bike, walk
    }

    public enum Purpose {
        shop, remote_work, leisure, escort_kids, escort_other
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

    public void setModeDefinition(Mode mode, long travelTimeBinSize, long minimumNumberOfObservationsPerDistribution, double cutoffSpeed) {
        this.travelTimeBinSize.put(mode, (double) travelTimeBinSize);
        this.minimumNumberOfObservationsPerDistribution.put(mode, minimumNumberOfObservationsPerDistribution);
        this.cutoffSpeed.put(mode, cutoffSpeed);
    }

    private long discretizeTravelTime(double traveTime, double travelTimeBinSize) {
        return (long) Math.floor(traveTime / travelTimeBinSize);
    }

    private double recoverTravelTime(long discretizedTravelTime, double travelTimeBinSize) {
        return discretizedTravelTime * travelTimeBinSize;
    }

    public void addObservation(double distance, double travelTime, Mode mode) {
        //if (distance / travelTime <= cutoffSpeed.get(mode)) {
            long discretizedTravelTime = discretizeTravelTime(travelTime, travelTimeBinSize.get(mode));
            Observation observation = new Observation(distance, travelTime, discretizedTravelTime, mode);
            observations.add(observation);
        //}
    }

    public long getNumberOfObservations() {
        return observations.size();
    }

    final private Map<Long, DistanceDistribution> cachedDistanceDistributions = new HashMap<>();

    final private Map<Mode, List<Double>> quantiles = new EnumMap<>(Mode.class);
    final private Map<Mode, List<DistanceDistribution>> distributions = new EnumMap<>(Mode.class);

    public void buildDistributions(Random random) {
        for (Mode mode : Mode.values()) {
            List<Observation> modeObservations = new LinkedList<>(
                    observations.stream().filter(obs -> obs.mode.equals(mode)).collect(Collectors.toList())
            );

            distributions.put(mode, new LinkedList<>());
            quantiles.put(mode, new LinkedList<>());

            int numberOfObservations = modeObservations.size();
            long numberOfBins = (int) Math.floor(numberOfObservations / minimumNumberOfObservationsPerDistribution.get(mode));

            Collections.sort(modeObservations, new Comparator<Observation>() {
                @Override
                public int compare(Observation o1, Observation o2) {
                    return Double.compare(o1.travelTime, o2.travelTime);
                }
            });

            List<Integer> ranks = new LinkedList<>();
            double currentRankValue = 0.0;

            for (int j = 0; j < numberOfBins; j++) {
                int rank = Math.min((int)(numberOfObservations * (j + 1) / numberOfBins), numberOfObservations - 1);

                if (modeObservations.get(rank).travelTime > currentRankValue) {
                    ranks.add(rank);
                    currentRankValue = modeObservations.get(rank).travelTime;
                }
            }

            System.err.println(ranks);

            for (int i = 0; i < ranks.size(); i++) {
                List<Observation> binObservations =
                        (i == 0) ? modeObservations.subList(0, ranks.get(i)) :
                                modeObservations.subList(ranks.get(i - 1), ranks.get(i));

                distributions.get(mode).add(new MicrocensusDistanceDistribution(random, binObservations.stream().map(obs -> obs.distance).collect(Collectors.toSet())));
                quantiles.get(mode).add(modeObservations.get(ranks.get(i)).travelTime);
            }

            logger.info("Creating distribution for " + mode + " ...");
            logger.info("   Number of observations: " + modeObservations.size());
            logger.info("   Number of bins: " + quantiles.get(mode).size());
            logger.info("   Thresholds: " + quantiles.get(mode));
            logger.info("");
        }
    }

    public DistanceDistribution createDistanceDistribution(Random random, Mode mode, double travelTime) {
        int index = 0;

        while (travelTime > quantiles.get(mode).get(index) && index < quantiles.get(mode).size() - 1) {
            index++;
        }

        return new MaximumSpeedDistribution(distributions.get(mode).get(index), travelTime, cutoffSpeed.get(mode), 1000);
    }

    public DistanceDistribution createDistanceDistribution2(Random random, Mode mode, double travelTime) {
        long discretizedTravelTime = discretizeTravelTime(travelTime, travelTimeBinSize.get(mode));
        Long hash = mode.ordinal() * 30 * 3600 + discretizedTravelTime;

        if (cachedDistanceDistributions.containsKey(hash)) {
            return new MaximumSpeedDistribution(cachedDistanceDistributions.get(hash), travelTime, cutoffSpeed.get(mode), 1000);
        }

        long minimumDiscretizedTravelTime = discretizedTravelTime;
        long maximumDiscretizedTravelTime = discretizedTravelTime + 1;

        Stream<Observation> modeFiltered = observations.stream()
                .parallel()
                .filter(obs -> obs.mode.equals(mode));

        long numberOfAvailableObservations = modeFiltered.count();

        long numberOfFilteredObservations = 0;
        Set<Double> distances = null;

        long minimumNumberOfObervations = minimumNumberOfObservationsPerDistribution.get(mode);

        long round = 0;

        while (numberOfFilteredObservations < Math.min(minimumNumberOfObervations, numberOfAvailableObservations)) {
            final long minDtt = minimumDiscretizedTravelTime;
            final long maxDtt = maximumDiscretizedTravelTime;

            distances = observations.stream()
                    .parallel()
                    .filter(obs -> obs.mode.equals(mode))
                    .filter(obs -> obs.discretizedTravelTime >= minDtt && obs.discretizedTravelTime < maxDtt)
                    .map(obs -> obs.distance)
                    .collect(Collectors.toSet());

            numberOfFilteredObservations = distances.size();

            if (round % 2 == 1) minimumDiscretizedTravelTime -= 1;
            if (round % 2 == 0) maximumDiscretizedTravelTime += 1;

            round++;
        }

        MicrocensusDistanceDistribution distanceDistribution = new MicrocensusDistanceDistribution(random, distances);
        cachedDistanceDistributions.put(hash, distanceDistribution);

        logger.info(String.format(
                "Create distances distances (%s, %s): %d observations (%s - %s)",
                mode.toString(), Time.writeTime(recoverTravelTime(discretizedTravelTime, travelTimeBinSize.get(mode))), distances.size(),
                Time.writeTime(recoverTravelTime(minimumDiscretizedTravelTime + 1, travelTimeBinSize.get(mode))),
                Time.writeTime(recoverTravelTime(maximumDiscretizedTravelTime - 1, travelTimeBinSize.get(mode)))
                ));

        return new MaximumSpeedDistribution(distanceDistribution, travelTime, cutoffSpeed.get(mode), 1000);
    }
}

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances;

public interface DistanceDistributionFactory {
    interface TripCharacteristics {}
    DistanceDistribution createDistribution(TripCharacteristics tripCharacteristics);
}

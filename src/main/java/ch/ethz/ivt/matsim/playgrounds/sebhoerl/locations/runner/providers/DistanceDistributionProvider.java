package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner.providers;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner.InputChainElement;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.distances.DistanceDistribution;

public interface DistanceDistributionProvider {
    DistanceDistribution provideDistanceDistribution(InputChainElement leg);
}

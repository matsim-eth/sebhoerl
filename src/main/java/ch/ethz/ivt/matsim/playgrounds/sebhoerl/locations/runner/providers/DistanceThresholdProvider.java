package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner.providers;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner.InputChainElement;

public interface DistanceThresholdProvider {
    double provideDistanceThreshold(InputChainElement leg);
}

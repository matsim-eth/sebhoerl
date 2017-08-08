package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner.providers;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.discretizer.LocationDiscretizer;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner.InputChainElement;

public interface LocationDiscretizerProvider {
    LocationDiscretizer provideLocationDiscretizer(InputChainElement leg);
}

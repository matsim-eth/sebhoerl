package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner;

import java.util.List;

public interface InputChain {
    List<InputChainElement> getElements();
    boolean hasVariableOriginLocation();
    boolean hasVariableDestinationLocation();
}

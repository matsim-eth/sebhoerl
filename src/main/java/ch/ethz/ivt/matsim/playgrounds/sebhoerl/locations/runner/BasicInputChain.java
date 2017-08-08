package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.runner;

import java.util.LinkedList;
import java.util.List;

public class BasicInputChain implements InputChain {
    final private List<InputChainElement> elements = new LinkedList<>();
    final private boolean hasVariableOriginLocation;
    final private boolean hasVariableDestinationLocation;

    public BasicInputChain(boolean hasVariableOriginLocation, boolean hasVariableDestinationLocation) {
        this.hasVariableOriginLocation = hasVariableOriginLocation;
        this.hasVariableDestinationLocation = hasVariableDestinationLocation;
    }

    @Override
    public List<InputChainElement> getElements() {
        return elements;
    }

    @Override
    public boolean hasVariableOriginLocation() {
        return hasVariableOriginLocation;
    }

    @Override
    public boolean hasVariableDestinationLocation() {
        return hasVariableDestinationLocation;
    }
}

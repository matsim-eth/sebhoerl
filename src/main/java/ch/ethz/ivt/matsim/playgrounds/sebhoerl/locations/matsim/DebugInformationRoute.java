package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver.DiscreteChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.ChainSamplerResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.matsim.api.core.v01.Id;
import org.matsim.core.population.routes.AbstractRoute;

public class DebugInformationRoute extends AbstractRoute {
    static public final String TYPE = "DebugInformation";
    private DebugInformation debugInformation;

    private class InfoObject {
        public long indexInChain;
        public long lengthOfChain;
        public boolean isDistanceChainSampledConverged;
        public long numberOfSamples;
        public long numberOfSolverIterations;
        public boolean isDiscreteChainSolverConverged;
        public long discreteIterations;
        public double discretizationError;
    }

    private InfoObject infoObject = new InfoObject();

    public DebugInformationRoute() {
        super(Id.createLinkId(""), Id.createLinkId(""));
    }

    public DebugInformationRoute(double travelTime, DebugInformation debugInformation) {
        this();
        this.setTravelTime(travelTime);
        this.setDistance(debugInformation.discretizedDistance);
        this.debugInformation = debugInformation;
    }

    @Override
    public String getRouteDescription() {
        return DebugInformation.write(debugInformation);
     }

    @Override
    public void setRouteDescription(String s) {
        debugInformation = DebugInformation.read(s);
    }

    @Override
    public String getRouteType() {
        return TYPE;
    }

    public DebugInformation getDebugInformation() {
        return debugInformation;
    }
}

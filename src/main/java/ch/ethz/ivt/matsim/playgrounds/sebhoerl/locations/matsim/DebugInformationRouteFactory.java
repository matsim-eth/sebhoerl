package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.population.routes.RouteFactory;

public class DebugInformationRouteFactory implements RouteFactory {
    @Override
    public Route createRoute(Id<Link> startLinkId, Id<Link> endLinkId) {
        return new DebugInformationRoute();
    }

    @Override
    public String getCreatedRouteType() {
        return DebugInformationRoute.TYPE;
    }
}

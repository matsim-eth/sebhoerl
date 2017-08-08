package ch.ethz.ivt.matsim.playgrounds.sebhoerl.pushsim;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.List;
import java.util.Queue;

public class PushVehicle {
    final private List<Id<Link>> links;

    public PushVehicle(List<Id<Link>> links) {
        this.links = links;
    }

    public Id<Link> getNextLinkId() {
        return links.get(0);
    }

    public void proceedLink() {
        System.out.println("Entering ");
        links.remove(0);
    }

    public boolean isArrivingOnNextLink() {
        return links.size() == 1;
    }

    public double getCapacityConsumption() {
        return 1.0;
    }
}

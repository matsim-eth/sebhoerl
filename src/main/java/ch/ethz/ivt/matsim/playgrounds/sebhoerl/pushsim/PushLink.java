package ch.ethz.ivt.matsim.playgrounds.sebhoerl.pushsim;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.*;
import java.util.concurrent.Semaphore;

public class PushLink {
    final private Object lock = new Object();

    final private List<PushVehicle> incoming = new LinkedList<>();
    final private List<PushVehicle> queue = new LinkedList<>();
    final private List<Double> exitTimes = new LinkedList<>();
    final private Map<Id<Link>, PushLink> links = new HashMap<>();

    double maximumCapacity = 0.0;
    double remainingCapacity = 0.0;
    double capacityGain = 0.0;
    double travelTime = 0.0;

    double lastUpdateTime = Double.NaN;

    final private Semaphore inputSemaphore = new Semaphore(0, true);

    public Semaphore getInputSemaphore() {
        return inputSemaphore;
    }

    public PushLink(double maximumCapacity, double capacityGain, double travelTime) {
        this.maximumCapacity = maximumCapacity;
        this.capacityGain = capacityGain;
        this.remainingCapacity = maximumCapacity;
        this.travelTime = travelTime;
    }

    public void addOutgoingLink(Id<Link> linkId, PushLink link) {
        links.put(linkId, link);
    }

    public void pushVehicle(PushVehicle vehicle) {
        synchronized (lock) {
            incoming.add(vehicle);
            remainingCapacity -= vehicle.getCapacityConsumption();
        }
    }

    private boolean hasCapacity(double additional) {
        synchronized (lock) {
            return remainingCapacity - additional >= 0.0;
        }
    }

    public void update(double time) {
        synchronized (lock) {
            while (remainingCapacity > 0.0 && incoming.size() > 0) {
                queue.add(incoming.remove(0));
                exitTimes.add(time + travelTime);
            }

            while (exitTimes.size() > 0 && exitTimes.get(0) >= time) {
                exitTimes.remove(0);

                PushVehicle vehicle = queue.remove(0);

                if (vehicle.isArrivingOnNextLink()) {
                    vehicle.proceedLink();
                } else {
                    PushLink nextLink = links.get(vehicle.getNextLinkId());

                    if (nextLink.hasCapacity(vehicle.getCapacityConsumption())) {
                        nextLink.pushVehicle(vehicle);
                    } else {
                        break;
                    }
                }
            }

            if (lastUpdateTime != Double.NaN) {
                remainingCapacity = Math.min(maximumCapacity, remainingCapacity + (time - lastUpdateTime) * capacityGain);
            }

            lastUpdateTime = time;
        }
    }
}

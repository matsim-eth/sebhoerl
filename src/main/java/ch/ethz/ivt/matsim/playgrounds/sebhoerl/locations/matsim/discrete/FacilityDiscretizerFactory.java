package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.discrete;

import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;

import java.util.*;

public class FacilityDiscretizerFactory {
    final private Random random;
    final private double radius;

    public FacilityDiscretizerFactory(Random random, double radius) {
        this.random = random;
        this.radius = radius;
    }

    public Map<String, FacilityDiscretizer> createDiscretizers(Collection<? extends ActivityFacility> facilities) {
        Map<String, Collection<FacilityLocation>> facilitiesByType = new HashMap<>();

        for (ActivityFacility facility : facilities) {
            for (ActivityOption option : facility.getActivityOptions().values()) {
                if (!facilitiesByType.containsKey(option.getType())) {
                    facilitiesByType.put(option.getType(), new LinkedList<>());
                }

                facilitiesByType.get(option.getType()).add(new FacilityLocation(facility));
            }
        }

        Map<String, FacilityDiscretizer> discretizers = new HashMap<>();

        for (Map.Entry<String, Collection<FacilityLocation>> entry : facilitiesByType.entrySet()) {
            discretizers.put(entry.getKey(), new FacilityDiscretizer(random, radius, entry.getValue()));
        }

        return discretizers;
    }
}

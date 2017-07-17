package ch.ethz.ivt.matsim.playgrounds.sebhoerl.utils;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.facilities.ActivityOptionImpl;
import org.matsim.facilities.MatsimFacilitiesReader;

import java.util.stream.Collectors;

public class Validate {
    static public void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

        new PopulationReader(scenario).readFile(args[0]);
        new MatsimFacilitiesReader(scenario).readFile(args[1]);

        for (Person person : scenario.getPopulation().getPersons().values()) {
            for (Activity activity : person.getSelectedPlan()
                    .getPlanElements().stream()
                    .filter(Activity.class::isInstance)
                    .map(Activity.class::cast)
                    .collect(Collectors.toList())) {
                String activityType = activity.getType();
                Id<ActivityFacility> facilityId = activity.getFacilityId();

                if (facilityId == null) {
                    System.err.println("No facility id: " + person.getId());
                    throw new RuntimeException();
                }

                ActivityFacility facility = scenario.getActivityFacilities().getFacilities().get(facilityId);

                if (facility == null) {
                    System.err.println("Unknown facility: " + facilityId.toString());
                    throw new RuntimeException();
                }

                if (facility.getActivityOptions().values().stream().filter(o -> o.getType().equals(activityType)).count() == 0) {
                    System.err.println("Person: " + person.getId());
                    System.err.println("Activity Type: " + activityType);
                    System.err.println("Facility: " + facilityId);
                    throw new RuntimeException();
                }
            }
        }
    }
}

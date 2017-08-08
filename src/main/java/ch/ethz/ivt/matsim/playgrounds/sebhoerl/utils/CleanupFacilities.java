package ch.ethz.ivt.matsim.playgrounds.sebhoerl.utils;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.*;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CleanupFacilities {
    static public void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile(args[0]);
        new MatsimFacilitiesReader(scenario).readFile(args[1]);

        Set<Id<ActivityFacility>> relevantIds = new HashSet<>();

        for (Person person : scenario.getPopulation().getPersons().values()) {
            for (PlanElement element : person.getSelectedPlan().getPlanElements()) {
                if (element instanceof Activity) {
                    relevantIds.add(((Activity) element).getFacilityId());
                }
            }
        }

        scenario.getActivityFacilities().getFacilities().values().removeIf(a -> !relevantIds.contains(a.getId()));

        for (ActivityFacility facility : scenario.getActivityFacilities().getFacilities().values()) {
            facility.getActivityOptions().clear();
            facility.addActivityOption(new ActivityOptionImpl("activity"));
        }

        new FacilitiesWriter(scenario.getActivityFacilities()).write(args[2]);
    }
}

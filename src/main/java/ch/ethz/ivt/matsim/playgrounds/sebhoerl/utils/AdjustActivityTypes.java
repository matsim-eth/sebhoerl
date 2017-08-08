package ch.ethz.ivt.matsim.playgrounds.sebhoerl.utils;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class AdjustActivityTypes {
    static public void main(String args[]) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile(args[0]);

        Random random = new Random();

        for (Person person : scenario.getPopulation().getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                List<Activity> activities = plan.getPlanElements().stream().filter(p -> p instanceof Activity).map(Activity.class::cast).collect(Collectors.toList());

                for (Activity activity : activities) {
                    activity.setType("activity");
                }
            }
        }

        new PopulationWriter(scenario.getPopulation()).write(args[1]);
    }
}

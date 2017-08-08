package ch.ethz.ivt.matsim.playgrounds.sebhoerl.utils;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class DiluteTimes {
    static public void main(String args[]) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile(args[0]);

        Random random = new Random();

        for (Person person : scenario.getPopulation().getPersons().values()) {
            Plan plan = person.getSelectedPlan();
            List<Activity> activities = person.getSelectedPlan().getPlanElements().stream().filter(p -> p instanceof Activity).map(Activity.class::cast).collect(Collectors.toList());

            double firstEndTime = activities.get(0).getEndTime();
            double minimumOffset = -firstEndTime;
            double offset = Math.max(minimumOffset, random.nextDouble() * 1200.0 - 600.0);

            for (PlanElement planElement : plan.getPlanElements()) {
                if (planElement instanceof Activity) {
                    Activity activity = (Activity) planElement;
                    activity.setEndTime(activity.getEndTime() + offset);
                }
            }
        }

        new PopulationWriter(scenario.getPopulation()).write(args[1]);
    }
}

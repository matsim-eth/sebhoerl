package ch.ethz.ivt.matsim.playgrounds.sebhoerl.utils;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class PreparePopulation {
    static public void main(String[] args) {
        Scenario referenceScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

        new PopulationReader(referenceScenario).readFile(args[0]);
        new PopulationReader(scenario).readFile(args[1]);

        Iterator<? extends Person> personIterator = scenario.getPopulation().getPersons().values().iterator();

        while (personIterator.hasNext()) {
            Person person = personIterator.next();
            Person referencePerson = referenceScenario.getPopulation().getPersons().get(person.getId());

            if (referencePerson != null) {
                List<Activity> activities = person.getSelectedPlan().getPlanElements().stream().filter(a -> a instanceof Activity).map(a -> (Activity) a).collect(Collectors.toList());
                Map<String, Long> counts = new HashMap<>();

                boolean delete = false;

                String firstActivityType = activities.get(0).getType();
                String lastActivityType = activities.get(activities.size() - 1).getType();

                for (Activity activity : activities) {
                    if (!counts.containsKey(activity.getType())) {
                        counts.put(activity.getType(), 0L);
                    }

                    counts.put(activity.getType(), counts.get(activity.getType()) + 1);
                    activity.setType(activity.getType() + "_" + counts.get(activity.getType()));

                    if (activity.getFacilityId() == null) {
                        System.err.println(person.getId() + " " + activity.getType());
                        delete = true;
                    }
                }

                if (firstActivityType.equals(lastActivityType)) {
                    activities.get(activities.size() - 1).setType(activities.get(0).getType());
                }

                if (delete) {
                    personIterator.remove();
                }
            } else {
                personIterator.remove();
            }
        }

        new PopulationWriter(scenario.getPopulation()).write(args[2]);

        for (Person person : scenario.getPopulation().getPersons().values()) {
            for (PlanElement element : person.getSelectedPlan().getPlanElements()) {
                if (element instanceof Leg) {
                    ((Leg) element).setRoute(null);
                }
            }
        }

        new PopulationWriter(scenario.getPopulation()).write(args[3]);
    }
}

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.utils;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Counter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PrepareInitialPopulation {
    static public void main(String[] args) {
        Scenario referenceScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

        new PopulationReader(referenceScenario).readFile(args[0]);
        new PopulationReader(scenario).readFile(args[1]);

        Iterator<? extends Person> personIterator = scenario.getPopulation().getPersons().values().iterator();

        Counter counter = new Counter("", " processed");

        while (personIterator.hasNext()) {
            Person person = personIterator.next();
            Person referencePerson = referenceScenario.getPopulation().getPersons().get(person.getId());

            if (referencePerson != null) {
                List<Activity> activities = person.getSelectedPlan().getPlanElements().stream().filter(a -> a instanceof Activity).map(a -> (Activity) a).collect(Collectors.toList());

                if (!activities.get(0).getType().equals("home") || !activities.get(activities.size() - 1).getType().equals("home")) {
                    personIterator.remove();
                }
            } else {
                personIterator.remove();
            }

            counter.incCounter();
        }

        new PopulationWriter(scenario.getPopulation()).write(args[2]);
    }
}

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.validation;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PreparePopulation {
    static public void main(String args[]) {
        String populationInputPath = args[0];
        String populationOutputPath = args[1];

        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile(populationInputPath);

        Iterator<? extends Person> personIterator = scenario.getPopulation().getPersons().values().iterator();

        while (personIterator.hasNext()) {
            Person person = personIterator.next();

            // Filter different first last
            Plan plan = person.getSelectedPlan();
            List<Activity> activities = plan.getPlanElements().stream().filter(Activity.class::isInstance).map(Activity.class::cast).collect(Collectors.toList());

            if (!activities.get(0).getType().equals("home") || !activities.get(activities.size() - 1).getType().equals("home")) {
                personIterator.remove();
                continue;
            }
        }

        new PopulationWriter(scenario.getPopulation()).write(populationOutputPath);
    }
}

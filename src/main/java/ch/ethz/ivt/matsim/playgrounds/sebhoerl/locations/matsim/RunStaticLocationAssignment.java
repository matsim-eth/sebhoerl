package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.discrete.FacilityDiscretizer;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.discrete.FacilityDiscretizerFactory;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.microcensus.Microcensus;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.microcensus.MicrocensusReader;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.MatsimFacilitiesReader;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class RunStaticLocationAssignment {
    static public void main(String[] args) throws IOException {
        String microcensusInputPath = args[0];

        Microcensus microcensus = new Microcensus(); //new Microcensus(300, 30.0 * 3600.0, 100);
        MicrocensusReader loader = new MicrocensusReader(microcensus);
        loader.read(new File(microcensusInputPath));

        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile(args[1]);
        new MatsimFacilitiesReader(scenario).readFile(args[2]);

        cleanupMatsimInput(scenario.getPopulation());

        Map<String, FacilityDiscretizer> discretizers = new FacilityDiscretizerFactory(new Random(0L), 50.0)
                .createDiscretizers(scenario.getActivityFacilities().getFacilities().values());


    }

    static private void cleanupMatsimInput(Population population) {
        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                for (PlanElement element : plan.getPlanElements()) {
                    if (element instanceof Activity) {
                        Activity activity = (Activity) element;

                        if (activity.getType().equals("shopping")) {
                            activity.setType("shop");
                        }

                        activity.setType(activity.getType().replaceAll("_[0-9]+$", ""));
                    }
                }
            }
        }
    }
}

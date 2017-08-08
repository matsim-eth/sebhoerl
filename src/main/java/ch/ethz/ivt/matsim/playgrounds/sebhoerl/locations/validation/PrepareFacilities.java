package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.validation;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.FacilitiesWriter;
import org.matsim.facilities.MatsimFacilitiesReader;

public class PrepareFacilities {
    static public void main(String[] args) {
        String facilitiesInputPath = args[0];
        String facilitiesOutputPath = args[1];

        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new MatsimFacilitiesReader(scenario).readFile(facilitiesInputPath);

        for (ActivityFacility facility : scenario.getActivityFacilities().getFacilities().values()) {
            facility.getActivityOptions().values().removeIf(o -> o.getType().matches(".+_[0-9]+$"));
        }

        new FacilitiesWriter(scenario.getActivityFacilities()).write(facilitiesOutputPath);
    }
}

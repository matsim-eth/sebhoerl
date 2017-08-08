package ch.ethz.ivt.matsim.playgrounds.sebhoerl.utils;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

public class RunTRB {
    static public void main(String[] args) {
        String configFile = args[0];

        // Configuration
        Config config = ConfigUtils.loadConfig(configFile);
        Scenario scenario = ScenarioUtils.loadScenario(config);

        // Controller setup

        Controler controler = new Controler(scenario);
        // Run

        controler.run();
    }
}

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.accessibility;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Counter;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.facilities.MatsimFacilitiesReader;

import java.io.*;

public class ExtractFacilities {
    static public void main(String[] args) throws IOException {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new MatsimFacilitiesReader(scenario).readFile(args[0]);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1])));
        writer.write("X\tY\tTYPE\n");

        Counter counter = new Counter("", "");

        for (ActivityFacility facility : scenario.getActivityFacilities().getFacilities().values()) {
            for (ActivityOption option : facility.getActivityOptions().values()) {
                if (option.getType().equals("work")) {
                    String[] row = {String.valueOf(facility.getCoord().getX()), String.valueOf(facility.getCoord().getY()), option.getType()};
                    writer.write(String.join("\t", row) + "\n");
                    writer.flush();
                }
            }

            counter.incCounter();
        }

        writer.close();
    }
}

package ch.ethz.ivt.matsim.playgrounds.sebhoerl.utils;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ShiftTimes {
	final private double interval;
	final private Random random;
	
	public ShiftTimes(double interval, Random random) {
		this.interval = interval;
		this.random = random;
	}
	
    public void apply(Population population) {
        for (Person person : population.getPersons().values()) {
            Plan plan = person.getSelectedPlan();
            List<Activity> activities = person.getSelectedPlan().getPlanElements().stream().filter(p -> p instanceof Activity).map(Activity.class::cast).collect(Collectors.toList());

            double firstEndTime = activities.get(0).getEndTime();
            double minimumOffset = -firstEndTime;
            double offset = Math.max(minimumOffset, random.nextDouble() * interval - 0.5 * interval);

            for (PlanElement planElement : plan.getPlanElements()) {
                if (planElement instanceof Activity) {
                    Activity activity = (Activity) planElement;
                    activity.setEndTime(activity.getEndTime() + offset);
                    activity.setStartTime(activity.getStartTime() + offset);
                }
            }
        }
    }
    
    static public void main(String args[]) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile(args[0]);
        
        double interval = args.length > 2 ? Double.parseDouble(args[2]) : 1200.0;
        new ShiftTimes(interval, new Random()).apply(scenario.getPopulation());
        
        new PopulationWriter(scenario.getPopulation()).write(args[1]);
    }
}
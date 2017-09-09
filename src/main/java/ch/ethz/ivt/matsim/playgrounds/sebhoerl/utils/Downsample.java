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

public class Downsample {
	final private double probability;
	final private Random random;
	
	public Downsample(double fraction, Random random) {
		this.probability = fraction;
		this.random = random;
	}
	
	public void run(Population population) {
        Iterator<? extends Person> personIterator = population.getPersons().values().iterator();

        while (personIterator.hasNext()) {
            personIterator.next();

            if (random.nextDouble() >= probability) {
                personIterator.remove();
            }
        }
	}
	
    static public void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile(args[0]);
        
        double probability = Double.parseDouble(args[1]);
        new Downsample(probability, new Random()).run(scenario.getPopulation());
        
        new PopulationWriter(scenario.getPopulation()).write(args[2]);
    }
}

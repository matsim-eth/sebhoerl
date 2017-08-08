package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TestRandom extends Random {
    final private List<Double> returnList;

    public TestRandom(List<Double> returnList) {
        this.returnList = new LinkedList<>(returnList);
    }

    @Override
    public double nextDouble() {
        return returnList.remove(0);
    }
}
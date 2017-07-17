package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.relaxation.TailGenerator;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TailGeneratorTest {
    @Test
    public void testTailGenerator() {
        TailGenerator tailSampler = new TailGenerator(new Random(0L));

        Vector2D anchorLocation = new Vector2D(50.0, 50.0);
        List<Double> distances = Arrays.asList(5.0, 10.0, 2.0);

        TailGenerator.Result result = tailSampler.sample(anchorLocation, distances);

        Assert.assertEquals(3, result.getLocations().size());
        Assert.assertEquals(5.0, result.getLocations().get(0).distance(anchorLocation), 1e-3);
        Assert.assertEquals(10.0, result.getLocations().get(0).distance(result.getLocations().get(1)), 1e-3);
        Assert.assertEquals(2.0, result.getLocations().get(1).distance(result.getLocations().get(2)), 1e-3);
    }
}

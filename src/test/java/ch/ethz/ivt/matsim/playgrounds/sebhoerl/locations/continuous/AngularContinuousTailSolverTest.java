package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.utils.TestRandom;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AngularContinuousTailSolverTest {


    @Test
    public void testAngularTailSolver() {
        AngularContinuousTailSolver solver = new AngularContinuousTailSolver(new TestRandom(Arrays.asList(90.0 / 360.0, 0.0)));

        Vector2D anchorLocation = new Vector2D(10.0, 10.0);
        List<Double> distances = Arrays.asList(12.0, 8.0);

        AngularContinuousTailSolver.Result result = solver.solve(anchorLocation, distances);

        Assert.assertEquals(new Vector2D(10.0, 22.0), result.getLocations().get(0));
        Assert.assertEquals(new Vector2D(18.0, 22.0), result.getLocations().get(1));
    }
}

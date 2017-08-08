package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.continuous;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class GravityContinuousChainSolverTest {
    @Test
    public void testTwoDistanceChain() {
        GravityContinuousChainSolver solver = new GravityContinuousChainSolver(0.1, 1e-6, 1000);

        List<Vector2D> initial = Arrays.asList(new Vector2D(20.0, 20.0));
        Vector2D origin = new Vector2D(0.0, 0.0);
        Vector2D destination = new Vector2D(100.0, 0.0);

        List<Double> distances;
        GravityContinuousChainSolver.Result result;

        // Circles do not intersect

        distances = Arrays.asList(2000.0, 20.0);
        result = solver.solve(origin, destination, distances, initial);
        Assert.assertFalse(result.isConverged());

        // Circle 2 is contained in 1

        distances = Arrays.asList(20.0, 20.0);
        result = solver.solve(origin, destination, distances, initial);
        Assert.assertFalse(result.isConverged());

        // Intersection is possible

        distances = Arrays.asList(100.0, 100.0);
        result = solver.solve(origin, destination, distances, initial);

        Assert.assertTrue(result.isConverged());

        Assert.assertEquals(50.0, result.getLocations().get(0).getX(), 1e-3);
        Assert.assertEquals(86.602540378, result.getLocations().get(0).getY(), 1e-3);
    }

    @Test
    public void testDirectLoop() {
        GravityContinuousChainSolver solver = new GravityContinuousChainSolver(0.1, 1e-1, 1000);
        Vector2D originDestination = new Vector2D(20.0, 20.0);

        ContinuousChainSolver.Result result = solver.solve(originDestination, originDestination, Arrays.asList(40.0, 40.0));
        Assert.assertTrue(result.isConverged());
        Assert.assertEquals(40.0, result.getLocations().get(0).distance(originDestination), 1e-3);
    }

    @Test
    public void testSameDistances() {
        GravityContinuousChainSolver solver = new GravityContinuousChainSolver(0.1, 1e-3, 1000);

        Vector2D origin = new Vector2D(2607386, 1126879);
        Vector2D destination = new Vector2D(2595358, 1122870);
        double distance = 6339.259913428381;

        ContinuousChainSolver.Result result = solver.solve(origin, destination, Arrays.asList(distance, distance));
        Assert.assertFalse(result.getLocations().get(0).isNaN());
    }

    @Test
    public void testGeneralDistanceChain() {
        GravityContinuousChainSolver solver = new GravityContinuousChainSolver(0.1, 1e-3, 1000);

        Vector2D origin = new Vector2D(0.0, 0.0);
        Vector2D destination = new Vector2D(100.0, 0.0);

        List<Double> distances;
        List<Vector2D> initial;
        GravityContinuousChainSolver.Result result;

        // Trapezoid
        distances = Arrays.asList(28.284271247461902, 60.0, 28.284271247461902);
        initial = Arrays.asList(new Vector2D(10.0, 1.0), new Vector2D(90.0, 1.0));
        result = solver.solve(origin, destination, distances, initial);
        Assert.assertTrue(result.isConverged());
        Assert.assertEquals(result.getLocations().size(), 2);
        Assert.assertEquals(distances.get(0), origin.distance(result.getLocations().get(0)), 1e-3);
        Assert.assertEquals(distances.get(1), result.getLocations().get(1).distance(result.getLocations().get(0)), 1e-3);
        Assert.assertEquals(distances.get(2), destination.distance(result.getLocations().get(1)), 1e-3);


        // Impossible Trapezoid
        distances = Arrays.asList(0.284271247461902, 1.0, 0.284271247461902);
        initial = Arrays.asList(new Vector2D(10.0, 1.0), new Vector2D(90.0, 1.0));
        result = solver.solve(origin, destination, distances, initial);
        Assert.assertFalse(result.isConverged());
    }
}

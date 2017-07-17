package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SetLocationDiscretizerTest {
    @Test
    public void testSetLocationDiscretizer() {
        BasicDiscreteLocation location1 = new BasicDiscreteLocation(new Vector2D(5.0, 0.0));
        BasicDiscreteLocation location2 = new BasicDiscreteLocation(new Vector2D(0.0, 10.0));
        List<DiscreteLocation> discreteLocations = Arrays.asList(location1, location2);

        SetLocationDiscretizer discretizer = new SetLocationDiscretizer(discreteLocations);

        DiscreteLocation result;

        result = discretizer.findDiscreteLocation(new Vector2D(2.0, 0.0));
        Assert.assertEquals(location1, result);

        result = discretizer.findDiscreteLocation(new Vector2D(6.0, 9.0));
        Assert.assertEquals(location2, result);
    }
}

package su.spb.den;

import org.junit.Assert;
import org.junit.Test;

public class ParametersTest {

    @Test
    public void valueWithoutParameterName() {
        Parameters parameters = new Parameters(new String[] { "foo" });
        Assert.assertFalse(parameters.isValid());
    }

    @Test
    public void invalidParameterName() {
        Parameters parameters = new Parameters(new String[] { "-", "foo" });
        Assert.assertFalse(parameters.isValid());
    }

    @Test
    public void unexpectedParameterName() {
        Parameters parameters = new Parameters(new String[] { "-foo", "bar" });
        Assert.assertFalse(parameters.isValid());
    }

    @Test
    public void allParametersAreCorrectlyParsed() {
        double delta = Double.MIN_VALUE;
        Parameters parameters = new Parameters(new String[] { "-floors", "7",
                "-height", "2.5", "-speed", "1.9", "-ent", "4" });
        Assert.assertEquals(7, parameters.getFloorNumbers());
        Assert.assertEquals(2.5d, parameters.getFloorHeight(), delta);
        Assert.assertEquals(1.9d, parameters.getSpeed(), delta);
        Assert.assertEquals(4, parameters.getEntranceTime());
        Assert.assertTrue(parameters.isValid());
    }

    // TODO : more tests, no time for this
}

package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;

public class PhysicalResourceGroupTest {

    @Test
    public void physicalResourceGroupToStringShouldContainName() {
        PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setName("My first group").create();

        assertThat(group.toString(), containsString("My first group"));
    }
}

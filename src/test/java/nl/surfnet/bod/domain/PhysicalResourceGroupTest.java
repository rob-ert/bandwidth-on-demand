package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;

public class PhysicalResourceGroupTest {

  @Test
  public void physicalResourceGroupToStringShouldContainName() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setName("My first group").create();

    assertThat(group.toString(), containsString("My first group"));
  }

  @Test
  public void physicalResourceGroupShouldCountItsPorts() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory()
        .addPhysicalPort(new PhysicalPortFactory().create(), new PhysicalPortFactory().create())
        .create();

    assertThat(group.getPhysicalPortCount(), is(2));
  }
}

package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import nl.surfnet.bod.support.InstituteFactory;

import org.junit.Test;


public class InstituteTest {

  @Test
  public void instituteShouldHaveNoAdminGroups() {
    Institute subject = new InstituteFactory().create();

    assertThat(subject.getAdminGroups(), hasSize(0));
  }
}

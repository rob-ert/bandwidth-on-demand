package nl.surfnet.bod.domain;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;

public class BodRoleTest {

  @Test
  public void nocShouldBeNoc() {
    BodRole nocEngineer = BodRole.createNocEngineer();

    assertThat(nocEngineer.isNocRole(), is(true));
    assertThat(nocEngineer.isManagerRole(), is(false));
  }

  @Test
  public void managerShouldBeManager() {
    BodRole manager = BodRole.createManager(new PhysicalResourceGroupFactory().create());

    assertThat(manager.isManagerRole(), is(true));
    assertThat(manager.isNocRole(), is(false));
  }

  @Test
  public void userShouldBeUser() {
    BodRole user = BodRole.createUser();

    assertThat(user.isUserRole(), is(true));
  }

  @Test
  public void newUserShouldNotBeUser() {
    BodRole newUser = BodRole.createNewUser();

    assertThat(newUser.isUserRole(), is(false));
  }

}

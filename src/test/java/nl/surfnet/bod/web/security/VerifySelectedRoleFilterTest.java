package nl.surfnet.bod.web.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.support.RichUserDetailsFactory;

import org.junit.Test;

public class VerifySelectedRoleFilterTest {

  private VerifySelectedRoleFilter subject = new VerifySelectedRoleFilter();

  @Test
  public void goToNocUriWithoutNocRole() {
    BodRole userRole = BodRole.createUser();
    RichUserDetails user = new RichUserDetailsFactory().addBodRoles(userRole).create();

    Security.setUserDetails(user);
    Security.switchToUser();

    subject.verifySelectedRole("/noc/reservations");

    assertThat(Security.isSelectedUserRole(), is(true));
    assertThat(Security.getSelectedRole(), is(userRole));
  }

  @Test
  public void goToNocUriWithNocRole() {
    BodRole userRole = BodRole.createUser();
    BodRole nocRole = BodRole.createNocEngineer();
    RichUserDetails user = new RichUserDetailsFactory().addBodRoles(userRole, nocRole).create();

    Security.setUserDetails(user);
    Security.switchToUser();

    subject.verifySelectedRole("/noc/reservations");

    assertThat(Security.isSelectedNocRole(), is(true));
    assertThat(Security.getSelectedRole(), is(nocRole));
  }

  @Test
  public void goToUserUri() {
    BodRole nocRole = BodRole.createNocEngineer();
    BodRole userRole = BodRole.createUser();
    RichUserDetails user = new RichUserDetailsFactory().addBodRoles(userRole, nocRole).create();

    Security.setUserDetails(user);
    Security.switchToNocEngineer();

    subject.verifySelectedRole("/reservations");

    assertThat(Security.isSelectedUserRole(), is(true));
    assertThat(Security.getSelectedRole(), is(userRole));
  }
}

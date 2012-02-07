package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import junit.framework.Assert;
import nl.surfnet.bod.support.ActivationEmailLinkFactory;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeUtils;
import org.junit.Test;

public class ActivationEmailLinkTest {

  private ActivationEmailLink<PhysicalResourceGroup> linkOne = new ActivationEmailLinkFactory<PhysicalResourceGroup>()
      .create();

  @Test
  public void shouldNoEmailSent() {
    ActivationEmailLink<PhysicalResourceGroup> link = new ActivationEmailLinkFactory<PhysicalResourceGroup>()
        .setEmailSent(false).create();

    assertThat(link.isEmailSent(), is(false));
    assertThat(link.getEmailSentDateTime(), nullValue());
  }

  @Test
  public void shouldEmailSent() {
    ActivationEmailLink<PhysicalResourceGroup> link = new ActivationEmailLinkFactory<PhysicalResourceGroup>()
        .setEmailSent(true).create();

    assertThat(link.isEmailSent(), is(true));
    assertThat(link.getEmailSentDateTime(), notNullValue());
  }

  @Test
  public void shouldActivate() {
    Long millis = DateMidnight.now().getMillis();

    DateTimeUtils.setCurrentMillisFixed(millis);

    assertThat(linkOne.isActivated(), is(false));
    assertThat(linkOne.getActivationDateTime(), nullValue());

    linkOne.activate();

    assertThat(linkOne.isActivated(), is(true));
    Assert.assertEquals(new Long(linkOne.getActivationDateTime().toDate().getTime()), millis);
  }

}

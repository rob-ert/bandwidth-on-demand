/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import nl.surfnet.bod.support.ActivationEmailLinkFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.AfterClass;
import org.junit.Test;

public class ActivationEmailLinkTest {

  private final ActivationEmailLink linkOne = new ActivationEmailLinkFactory()
      .create();

  /**
   * Set system time back to prevent influencing other test cases
   */
  @AfterClass
  public static void tearDown() {
    DateTimeUtils.setCurrentMillisSystem();
  }

  @Test
  public void shouldHaveUUID() {
    assertThat(linkOne.getUuid(), notNullValue());
  }

  @Test
  public void shouldSameIdSourceObjectIdAndSourceId() {
    assertThat(linkOne.getSourceId(), equalTo(linkOne.getSourceObject().getId()));
  }

  @Test
  public void shouldNoEmailSent() {
    ActivationEmailLink link = new ActivationEmailLinkFactory()
        .setEmailSent(false).create();

    assertThat(link.isEmailSent(), is(false));
    assertThat(link.getEmailSentDateTime(), nullValue());
  }

  @Test
  public void shouldEmailSent() {
    Long millis = DateMidnight.now().getMillis();
    DateTimeUtils.setCurrentMillisFixed(millis);

    ActivationEmailLink link = new ActivationEmailLinkFactory().setEmailSent(false).create();

    assertThat(link.isEmailSent(), is(false));
    assertThat(link.getEmailSentDateTime(), nullValue());

    link.emailWasSent();

    assertThat(link.isEmailSent(), is(true));
    assertThat(link.getEmailSentDateTime().toDate().getTime(), is(millis));

    DateTimeUtils.setCurrentMillisSystem();
  }

  @Test
  public void shouldActivate() {
    Long millis = DateMidnight.now().getMillis();
    DateTimeUtils.setCurrentMillisFixed(millis);

    assertThat(linkOne.isActivated(), is(false));
    assertThat(linkOne.getActivationDateTime(), nullValue());

    linkOne.activate();

    assertThat(linkOne.isActivated(), is(true));
    assertEquals(new Long(linkOne.getActivationDateTime().toDate().getTime()), millis);

    DateTimeUtils.setCurrentMillisSystem();
  }

  @Test
  public void shouldBeValid() {
    assertThat(linkOne.isValid(), is(true));
  }

  @Test
  public void shouldNotBeValidWhenNoEmailWasSent() {
    ActivationEmailLink link = new ActivationEmailLinkFactory()
        .setEmailSent(false).create();

    assertThat(link.isValid(), is(false));
  }

  @Test
  public void shouldNotBeValidWhenAlreadyActviated() {

    linkOne.activate();
    assertThat(linkOne.isValid(), is(false));
  }

  @Test
  public void shouldExpire() {
    ActivationEmailLink link = new ActivationEmailLinkFactory().create();
    link.setEmailSentDateTime(DateTime.now().minusDays(ActivationEmailLink.VALID_PERIOD_DAYS + 1));

    assertThat(link.isValid(), is(false));
  }

  @Test
  public void shouldStillBeValid() {
    ActivationEmailLink link = new ActivationEmailLinkFactory().create();
    link.setEmailSentDateTime(DateTime.now());

    assertThat(link.isValid(), is(true));
  }

  @Test
  public void getAdminGroups() {
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().setAdminGroup("urn:ict-managers").create();
    ActivationEmailLink subject = new ActivationEmailLinkFactory().setPhysicalResourceGroup(prg).create();

    assertThat(subject.getAdminGroups(), hasSize(1));
    assertThat(subject.getAdminGroups(), contains("urn:ict-managers"));
  }

}

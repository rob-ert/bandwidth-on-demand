/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.lang.reflect.Field;

import junit.framework.Assert;
import nl.surfnet.bod.support.ActivationEmailLinkFactory;

import org.hibernate.annotations.common.reflection.ReflectionUtil;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDateTime;
import org.junit.AfterClass;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

public class ActivationEmailLinkTest {

  /**
   * Set system time back to prevent influencing other testcases
   */
  @AfterClass
  public static void tearDown() {
    DateTimeUtils.setCurrentMillisSystem();
  }

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

    DateTimeUtils.setCurrentMillisSystem();
  }

  @Test
  public void shouldBeValid() {
    assertThat(linkOne.isValid(), is(true));
  }

  @Test
  public void shouldNotBeValidWhenNoEmailWasSent() {
    ActivationEmailLink<PhysicalResourceGroup> link = new ActivationEmailLinkFactory<PhysicalResourceGroup>()
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
    ActivationEmailLink<PhysicalResourceGroup> link = new ActivationEmailLinkFactory<PhysicalResourceGroup>().create();
    link.setEmailSentDateTime(LocalDateTime.now().minusDays(ActivationEmailLink.VALID_PERIOD_DAYS + 1));

    assertThat(link.isValid(), is(false));
  }
  
  @Test
  public void shouldStillBeValid() {
    ActivationEmailLink<PhysicalResourceGroup> link = new ActivationEmailLinkFactory<PhysicalResourceGroup>().create();
    link.setEmailSentDateTime(LocalDateTime.now());

    assertThat(link.isValid(), is(true));
  }

}

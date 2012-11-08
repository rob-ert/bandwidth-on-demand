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
package nl.surfnet.bod.db.migration;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class V1_6_0_2__MigrateLogEventsTest {

  private final V1_6_0_2__MigrateLogEvents subject = new V1_6_0_2__MigrateLogEvents();

  @Test
  public void testSplitAtSemiColonWithMultipleColons() {
    String[] parts = subject.splitAtColon("Reservation: abc:123");
    assertThat(parts.length, is(2));
    assertThat(parts[0], is("Reservation"));
    assertThat(parts[1], is("abc:123"));
  }

  @Test
  public void testSplitAtSemiColonWithSingleColons() {
    String[] parts = subject.splitAtColon("Reservation: abc123");

    assertThat(parts.length, is(2));
    assertThat(parts[0], is("Reservation"));
    assertThat(parts[1], is("abc123"));
  }

  @Test
  public void testSplitAtSemiColonWithNoneColons() {
    String[] parts = subject.splitAtColon("Reservation abc123");

    assertThat(parts, nullValue());
  }

  @Test
  public void testSplitAtSemiColonWithEmptySecondPart() {
    String[] parts = subject.splitAtColon("Reservation:");

    assertThat(parts.length, is(2));
    assertThat(parts[0], is("Reservation"));
    assertThat(parts[1], is(""));
  }

  @Test
  public void testSplitAtSemiColonWithSpaceAsSecondPart() {
    String[] parts = subject.splitAtColon("Reservation: ");

    assertThat(parts.length, is(2));
    assertThat(parts[0], is("Reservation"));
    assertThat(parts[1], is(""));
  }

  @Test
  public void testSplitAtSemiColonEmpty() {
    String[] parts = subject.splitAtColon("");

    assertThat(parts, nullValue());
  }

  @Test
  public void testSplitAtSemiColonNull() {
    String[] parts = subject.splitAtColon(null);

    assertThat(parts, nullValue());
  }
}

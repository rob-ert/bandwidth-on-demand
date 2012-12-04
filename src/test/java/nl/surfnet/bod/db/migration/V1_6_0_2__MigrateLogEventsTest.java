/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

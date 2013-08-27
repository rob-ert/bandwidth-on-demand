/**
 * Copyright (c) 2012, 2013 SURFnet BV
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

import static nl.surfnet.bod.domain.VlanRangesValidator.parseRanges;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Random;

import javax.validation.ConstraintValidatorContext;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class VlanRangesValidatorTest {

  @Mock private ConstraintValidatorContext context;

  private final VlanRangesValidator subject = new VlanRangesValidator();

  @Test
  public void should_ignore_empty_value() {
    assertThat(subject.isValid(null, context), is(true));
    assertThat(subject.isValid("", context), is(true));
  }

  @Test
  public void should_fail_on_invalid_ranges() {
    assertThat(subject.isValid("abc", context), is(false));
  }

  @Test
  public void should_pass_on_valid_ranges() {
    assertThat(subject.isValid("1 - 1000", context), is(true));
  }

  @Test
  public void should_support_single_vlan_for_evpl_port() {
    assertThat(parseRanges("1"), is(ImmutableRangeSet.of(Range.singleton(1))));
  }

  @Test
  public void should_support_single_vlan_range_for_evpl_port() {
    assertThat(parseRanges("2-1000"), is(ImmutableRangeSet.of(Range.closed(2, 1000))));
  }

  @Test
  public void should_support_multiple_vlan_ranges_for_evpl_port() {
    assertThat(parseRanges("1,100-1000,2000-2001"), is(ImmutableRangeSet.<Integer>builder().add(Range.singleton(1)).add(Range.closed(100, 1000)).add(Range.closed(2000, 2001)).build()));
  }

  @Test
  public void should_reject_random_strings() {
    Random random = new Random();
    for (int i = 0; i < 5000; ++i) {
      int count = random.nextInt(42);
      subject.isValid(RandomStringUtils.random(count), context);
      subject.isValid(RandomStringUtils.randomAlphanumeric(count), context);
    }
  }

  @Test
  public void should_not_allow_empty_vlan_range_for_evpl_port() {
    try {
      parseRanges("1000-100");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), is("lower bound 1000 cannot be greater than upper bound 100"));
    }
  }

  @Test
  public void should_not_allow_out_of_range_vlan_id() {
    try {
      parseRanges("0");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), is("VLAN ID must be between 1 and 4095"));
    }
  }
}

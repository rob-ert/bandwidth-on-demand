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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

import org.springframework.util.StringUtils;

public class VlanRangesValidator implements ConstraintValidator<VlanRanges, String> {

  public static final int MINIMUM_VLAN_ID = 1;
  public static final int MAXIMUM_VLAN_ID = 4095;

  @Override
  public void initialize(VlanRanges constraintAnnotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (StringUtils.isEmpty(value)) {
      return true;
    }
    try {
      parseRanges(value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static ImmutableRangeSet<Integer> parseRanges(String vlanRanges) {
    ImmutableRangeSet.Builder<Integer> builder = ImmutableRangeSet.builder();
    for (String range: vlanRanges.split(",")) {
      String[] xs = range.split("-");
      if (xs.length == 1) {
        builder.add(Range.singleton(parseVlanId(xs[0])));
      } else if (xs.length == 2) {
        int lower = parseVlanId(xs[0]);
        int upper = parseVlanId(xs[1]);
        if (lower > upper) {
          throw new IllegalArgumentException("lower bound " + lower + " cannot be greater than upper bound " + upper);
        }
        builder.add(Range.closed(lower, upper));
      } else {
        throw new IllegalArgumentException("invalid range " + vlanRanges);
      }
    }
    return builder.build();
  }

  private static int parseVlanId(String s) {
    int result = Integer.parseInt(s.trim());
    if (result < MINIMUM_VLAN_ID || result > MAXIMUM_VLAN_ID) {
      throw new IllegalArgumentException("VLAN ID must be between " + MINIMUM_VLAN_ID + " and " + MAXIMUM_VLAN_ID);
    }
    return result;
  }
}

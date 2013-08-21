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

import java.util.Collection;
import java.util.Collections;

import javax.persistence.Entity;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import nl.surfnet.bod.nsi.NsiConstants;

import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class EnniPort extends PhysicalPort {

  @NotEmpty
  private String inboundPeer;

  @NotEmpty
  private String outboundPeer;

  @VlanRanges
  private String vlanRanges;

  public EnniPort() {
  }

  public EnniPort(NbiPort nbiPort) {
    super(nbiPort);
  }

  public String getInboundPeer() {
    return inboundPeer;
  }

  public void setInboundPeer(String inboundPeer) {
    this.inboundPeer = inboundPeer;
  }

  public String getOutboundPeer() {
    return outboundPeer;
  }

  public void setOutboundPeer(String outboundPeer) {
    this.outboundPeer = outboundPeer;
  }

  public String getVlanRanges() {
    return vlanRanges;
  }

  public void setVlanRanges(String vlanRanges) {
    this.vlanRanges = vlanRanges;
  }

  @Override
  public Collection<String> getAdminGroups() {
    return Collections.emptyList();
  }

  public String getNsiStpIdV2() {
    return NsiConstants.URN_STP_V2 + ":" + getBodPortId();
  }

  public boolean isVlanIdAllowed(int vlan) {
    if (!isVlanRequired()) {
      return false;
    }

    RangeSet<Integer> range = parseRanges(vlanRanges);
    return range.contains(vlan);
  }

  private RangeSet<Integer> parseRanges(String vlanRanges2) {
    ImmutableRangeSet.Builder<Integer> builder = ImmutableRangeSet.builder();
    for (String range: vlanRanges.split(",")) {
      String[] xs = range.split("-");
      if (xs.length == 1) {
        builder.add(Range.singleton(Integer.parseInt(xs[0].trim())));
      } else if (xs.length == 2) {
        int lower = Integer.parseInt(xs[0].trim());
        int upper = Integer.parseInt(xs[1].trim());
        if (lower > upper) {
          throw new IllegalArgumentException("lower bound " + lower + " cannot be greater than upper bound " + upper);
        }
        builder.add(Range.closed(lower, upper));
      } else {
        throw new IllegalArgumentException("invalid range " + vlanRanges2);
      }
    }
    return builder.build();
  }

}

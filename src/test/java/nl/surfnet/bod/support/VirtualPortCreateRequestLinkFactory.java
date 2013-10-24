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
package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.AbstractRequestLink.RequestStatus;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPortCreateRequestLink;
import nl.surfnet.bod.domain.VirtualResourceGroup;

import org.joda.time.DateTime;

public class VirtualPortCreateRequestLinkFactory {

  private VirtualResourceGroup virtualResourceGroup = new VirtualResourceGroupFactory().create();
  private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();
  private final DateTime requestDateTime = DateTime.now();
  private final String requestorUrn = "urn:truusvisscher";
  private final String requestorName = "Truus Visscher";
  private final String requestorEmail = "truus@visscher.nl";
  private String uuid;
  private String message = "I would like to have a new virtual port to do my work.";
  private Long minBandwidth = 1000L;
  private RequestStatus status = RequestStatus.PENDING;

  public VirtualPortCreateRequestLink create() {
    VirtualPortCreateRequestLink link = new VirtualPortCreateRequestLink();

    link.setVirtualResourceGroup(virtualResourceGroup);
    link.setPhysicalResourceGroup(physicalResourceGroup);
    link.setRequestDateTime(requestDateTime);
    link.setRequestorUrn(requestorUrn);
    link.setRequestorEmail(requestorEmail);
    link.setRequestorName(requestorName);

    if (uuid != null) {
      link.setUuid(uuid);
    }
    link.setMessage(message);
    link.setMinBandwidth(minBandwidth);
    link.setStatus(status);

    return link;
  }

  public VirtualPortCreateRequestLinkFactory setMessage(String message) {
    this.message = message;
    return this;
  }

  public VirtualPortCreateRequestLinkFactory setMinBandwidth(Long minBandwidth) {
    this.minBandwidth = minBandwidth;
    return this;
  }

  public VirtualPortCreateRequestLinkFactory setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
    this.virtualResourceGroup = virtualResourceGroup;
    return this;
  }

  public VirtualPortCreateRequestLinkFactory setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
    return this;
  }

  public VirtualPortCreateRequestLinkFactory setStatus(RequestStatus status) {
    this.status = status;
    return this;
  }
}

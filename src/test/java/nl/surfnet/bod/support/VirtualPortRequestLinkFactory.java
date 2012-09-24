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
package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.domain.VirtualPortRequestLink.RequestStatus;
import nl.surfnet.bod.domain.VirtualResourceGroup;

import org.joda.time.DateTime;

public class VirtualPortRequestLinkFactory {

  private VirtualResourceGroup virtualResourceGroup = new VirtualResourceGroupFactory().create();
  private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();
  private final DateTime requestDateTime = DateTime.now();
  private final String requestorUrn = "urn:truusvisscher";
  private final String requestorName = "Truus Visscher";
  private final String requestorEmail = "truus@visscher.nl";
  private String uuid;
  private String message = "I would like to have a new virtual port to do my work.";
  private Integer minBandwidth = 1000;
  private RequestStatus status = RequestStatus.PENDING;

  public VirtualPortRequestLink create() {
    VirtualPortRequestLink link = new VirtualPortRequestLink();

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

  public VirtualPortRequestLinkFactory setMessage(String message) {
    this.message = message;
    return this;
  }

  public VirtualPortRequestLinkFactory setMinBandwidth(Integer minBandwidth) {
    this.minBandwidth = minBandwidth;
    return this;
  }

  public VirtualPortRequestLinkFactory setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
    this.virtualResourceGroup = virtualResourceGroup;
    return this;
  }

  public VirtualPortRequestLinkFactory setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
    return this;
  }

  public VirtualPortRequestLinkFactory setStatus(RequestStatus status) {
    this.status = status;
    return this;
  }
}

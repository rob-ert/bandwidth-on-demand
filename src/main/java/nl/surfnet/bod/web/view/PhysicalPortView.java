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
package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.NmsAlignmentStatus;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.nsi.NsiHelper;

public class PhysicalPortView {

  private final Long id;
  private final String managerLabel;
  private final String nocLabel;
  private final String bodPortId;
  private final String instituteName;
  private final String nmsPortId;
  private final ElementActionView deleteActionView;
  private final Long numberOfVirtualPorts;
  private final boolean vlanRequired;
  private boolean alignedWithNMS;
  private final NmsAlignmentStatus nmsAlignmentStatus;
  private boolean deleteRender;

  private final String nmsNeId;
  private final String nmsPortSpeed;
  private final String nmsSapName;
  private final String interfaceType;

  private final String outboundPeer;
  private final String inboundPeer;
  private final String vlanRanges;

  private int reservationsAmount;
  private String nsiStpIdV2;
  private String nsiStpIdV1;

  public String getInterfaceType() {
    return interfaceType;
  }

  public PhysicalPortView(NbiPort nbiPort) {
    this.id = null;
    this.managerLabel = nbiPort.getSuggestedNocLabel();
    this.instituteName = null;
    this.vlanRequired = nbiPort.isVlanRequired();
    this.alignedWithNMS = true;
    this.nmsAlignmentStatus = null;
    this.interfaceType = nbiPort.getInterfaceType().toString();

    this.numberOfVirtualPorts = 0L;
    this.deleteActionView = null;
    this.deleteRender = deleteActionView == null ? false : true;

    this.nmsNeId = nbiPort.getNmsNeId();
    this.nmsPortSpeed = nbiPort.getNmsPortSpeed();
    this.nmsSapName = nbiPort.getNmsSapName();

    this.inboundPeer = null;
    this.outboundPeer = null;
    this.vlanRanges = null;

    this.nmsPortId = nbiPort.getNmsPortId();
    this.nocLabel = nbiPort.getSuggestedNocLabel();
    this.bodPortId = nbiPort.getSuggestedBodPortId();
  }

  public PhysicalPortView(EnniPort enniPort, ElementActionView deleteActionView, NsiHelper nsiHelper) {
    this.id = enniPort.getId();
    this.nocLabel = enniPort.getNocLabel();
    this.bodPortId = enniPort.getBodPortId();
    this.nmsPortId = enniPort.getNmsPortId();
    this.vlanRequired = enniPort.isVlanRequired();
    this.alignedWithNMS = enniPort.isAlignedWithNMS();
    this.nmsAlignmentStatus = enniPort.getNmsAlignmentStatus();

    this.numberOfVirtualPorts = 0L;
    this.managerLabel = null;
    this.instituteName = null;
    this.deleteActionView = deleteActionView;
    this.deleteRender = deleteActionView == null ? false : true;

    this.inboundPeer = enniPort.getInboundPeer();
    this.outboundPeer = enniPort.getOutboundPeer();
    this.vlanRanges = enniPort.getVlanRanges();
    this.nsiStpIdV2 = nsiHelper.getStpIdV2(enniPort);
    this.nsiStpIdV1 = nsiHelper.getStpIdV1(enniPort);

    this.nmsNeId = enniPort.getNbiPort().getNmsNeId();
    this.nmsPortSpeed = enniPort.getNbiPort().getNmsPortSpeed();
    this.nmsSapName = enniPort.getNbiPort().getNmsSapName();
    this.interfaceType = enniPort.getNbiPort().getInterfaceType().toString();
  }

  public PhysicalPortView(UniPort physicalPort, ElementActionView deleteActionView, long virtualPortSize) {
    this.id = physicalPort.getId();
    this.managerLabel = physicalPort.getManagerLabel();
    this.nocLabel = physicalPort.getNocLabel();
    this.bodPortId = physicalPort.getBodPortId();
    this.instituteName = physicalPort.getPhysicalResourceGroup() == null ? null : physicalPort.getPhysicalResourceGroup().getName();
    this.nmsPortId = physicalPort.getNmsPortId();
    this.vlanRequired = physicalPort.isVlanRequired();
    this.alignedWithNMS = physicalPort.isAlignedWithNMS();
    this.nmsAlignmentStatus = physicalPort.getNmsAlignmentStatus();

    this.numberOfVirtualPorts = virtualPortSize;
    this.deleteActionView = deleteActionView;
    this.deleteRender = deleteActionView == null ? false : true;

    this.inboundPeer = null;
    this.outboundPeer = null;
    this.vlanRanges = null;

    this.nmsNeId = physicalPort.getNbiPort().getNmsNeId();
    this.nmsPortSpeed = physicalPort.getNbiPort().getNmsPortSpeed();
    this.nmsSapName = physicalPort.getNbiPort().getNmsSapName();
    this.interfaceType = physicalPort.getNbiPort().getInterfaceType().toString();
  }

  public PhysicalPortView(UniPort physicalPort, ElementActionView deleteActionView) {
    this(physicalPort, deleteActionView, 0);
  }

  public PhysicalPortView(UniPort physicalPort) {
    this(physicalPort, new ElementActionView(false, ""), 0L);
  }

  public String getNsiStpIdV2() {
    return nsiStpIdV2;
  }

  public String getNsiStpIdV1() {
    return nsiStpIdV1;
  }

  public String getDeleteReasonKey() {
    return deleteActionView == null ? "" : deleteActionView.getReasonKey();
  }

  public boolean isDeleteAllowed() {
    return deleteActionView == null ? false : deleteActionView.isAllowed();
  }

  public long getId() {
    return id;
  }

  public String getNocLabel() {
    return nocLabel;
  }

  public String getBodPortId() {
    return bodPortId;
  }

  public String getInstituteName() {
    return instituteName;
  }

  public String getNmsPortId() {
    return nmsPortId;
  }

  public ElementActionView getDeleteActionView() {
    return deleteActionView;
  }

  public String getManagerLabel() {
    return managerLabel;
  }

  public long getNumberOfVirtualPorts() {
    return numberOfVirtualPorts;
  }

  public boolean isDeleteRender() {
    return deleteRender;
  }

  public boolean isVlanRequired() {
    return vlanRequired;
  }

  public boolean isAlignedWithNMS() {
    return alignedWithNMS;
  }

  public NmsAlignmentStatus getNmsAlignmentStatus() {
    return nmsAlignmentStatus;
  }

  public final String getNmsNeId() {
    return nmsNeId;
  }

  public final String getNmsPortSpeed() {
    return nmsPortSpeed;
  }

  public final String getNmsSapName() {
    return nmsSapName;
  }

  public final int getReservationsAmount() {
    return reservationsAmount;
  }

  public final void setReservationsAmount(int reservationsAmount) {
    this.reservationsAmount = reservationsAmount;
  }

  public final void setDeleteRender(boolean deleteRender) {
    this.deleteRender = deleteRender;
  }

  public String getOutboundPeer() {
    return outboundPeer;
  }

  public String getInboundPeer() {
    return inboundPeer;
  }

  public String getVlanRanges() {
    return vlanRanges;
  }

  @Override
  public String toString() {
    return "PhysicalPortView [id=" + id + ", managerLabel=" + managerLabel + ", nocLabel=" + nocLabel + ", bodPortId="
        + bodPortId + ", instituteName=" + instituteName + ", nmsPortId=" + nmsPortId
        + ", deleteActionView=" + deleteActionView + ", numberOfVirtualPorts=" + numberOfVirtualPorts
        + ", vlanRequired=" + vlanRequired + ", nmsAlignmentStatus=" + nmsAlignmentStatus + ", deleteRender=" + deleteRender
        + "]";
  }
}

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

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.NmsAlignmentStatus;
import nl.surfnet.bod.domain.UniPort;

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

  private int reservationsAmount;

  public PhysicalPortView(NbiPort nbiPort) {
    this.id = null;
    this.managerLabel = nbiPort.getSuggestedNocLabel();
    this.instituteName = null;
    this.vlanRequired = nbiPort.isVlanRequired();
    this.alignedWithNMS = true;
    this.nmsAlignmentStatus = null;

    this.numberOfVirtualPorts = 0L;
    this.deleteActionView = null;
    this.deleteRender = deleteActionView == null ? false : true;

    this.nmsNeId = nbiPort.getNmsNeId();
    this.nmsPortSpeed = nbiPort.getNmsPortSpeed();
    this.nmsSapName = nbiPort.getNmsSapName();

    this.nmsPortId = nbiPort.getNmsPortId();
    this.nocLabel = nbiPort.getSuggestedNocLabel();
    this.bodPortId = nbiPort.getSuggestedBodPortId();
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

    this.nmsNeId = physicalPort.getNbiPort().getNmsNeId();
    this.nmsPortSpeed = physicalPort.getNbiPort().getNmsPortSpeed();
    this.nmsSapName = physicalPort.getNbiPort().getNmsSapName();
  }

  public PhysicalPortView(UniPort physicalPort, ElementActionView deleteActionView) {
    this(physicalPort, deleteActionView, 0);
  }

  public PhysicalPortView(UniPort physicalPort) {
    this(physicalPort, new ElementActionView(false, ""), 0L);
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

  @Override
  public String toString() {
    return "PhysicalPortView [id=" + id + ", managerLabel=" + managerLabel + ", nocLabel=" + nocLabel + ", bodPortId="
        + bodPortId + ", instituteName=" + instituteName + ", nmsPortId=" + nmsPortId
        + ", deleteActionView=" + deleteActionView + ", numberOfVirtualPorts=" + numberOfVirtualPorts
        + ", vlanRequired=" + vlanRequired + ", nmsAlignmentStatus=" + nmsAlignmentStatus + ", deleteRender=" + deleteRender
        + "]";
  }
}

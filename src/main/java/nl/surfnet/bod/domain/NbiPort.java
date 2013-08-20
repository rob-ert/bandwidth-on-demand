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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.hibernate.search.annotations.Field;
import org.hibernate.validator.constraints.NotEmpty;

@Embeddable
public class NbiPort {

  public enum InterfaceType {
    E_NNI, UNI, UNKNOWN
  }

  @NotEmpty
  @Column(unique = true)
  @Field
  private String nmsPortId;

  @Field
  private String nmsNeId;

  @Field
  private String nmsPortSpeed;

  @Field
  private String nmsSapName;

  private String signalingType;

  @Enumerated(EnumType.STRING)
  private InterfaceType interfaceType;

  private String supportedServiceType;

  @Transient
  private String suggestedNocLabel;

  @Transient
  private String suggestedBodPortId;

  private boolean vlanRequired;

  public String getNmsPortId() {
    return nmsPortId;
  }

  public void setNmsPortId(String nmsPortId) {
    this.nmsPortId = nmsPortId;
  }

  public String getNmsNeId() {
    return nmsNeId;
  }

  public void setNmsNeId(String nmsNeId) {
    this.nmsNeId = nmsNeId;
  }

  public String getNmsPortSpeed() {
    return nmsPortSpeed;
  }

  public void setNmsPortSpeed(String nmsPortSpeed) {
    this.nmsPortSpeed = nmsPortSpeed;
  }

  public String getNmsSapName() {
    return nmsSapName;
  }

  public void setNmsSapName(String nmsSapName) {
    this.nmsSapName = nmsSapName;
  }

  public String getSignalingType() {
    return signalingType;
  }

  public void setSignalingType(String signalingType) {
    this.signalingType = signalingType;
  }

  public String getSupportedServiceType() {
    return supportedServiceType;
  }

  public void setSupportedServiceType(String supportedServiceType) {
    this.supportedServiceType = supportedServiceType;
  }

  public boolean isVlanRequired() {
    return vlanRequired;
  }

  public void setVlanRequired(boolean vlanRequired) {
    this.vlanRequired = vlanRequired;
  }

  public String getSuggestedNocLabel() {
    return suggestedNocLabel;
  }

  public void setSuggestedNocLabel(String suggestedNocLabel) {
    this.suggestedNocLabel = suggestedNocLabel;
  }

  public String getSuggestedBodPortId() {
    return suggestedBodPortId;
  }

  public void setSuggestedBodPortId(String suggestedBodPortId) {
    this.suggestedBodPortId = suggestedBodPortId;
  }

  public InterfaceType getInterfaceType() {
    return interfaceType;
  }

  public void setInterfaceType(InterfaceType interfaceType) {
    this.interfaceType = interfaceType;
  }

  @Override
  public String toString() {
    return "NbiPort [nmsPortId=" + nmsPortId + ", nmsNeId=" + nmsNeId + ", nmsPortSpeed=" + nmsPortSpeed
        + ", nmsSapName=" + nmsSapName + ", signalingType=" + signalingType + ", supportedServiceType="
        + supportedServiceType + ", vlanRequired=" + vlanRequired + "]";
  }

}

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

import java.util.UUID;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.NbiPort.InterfaceType;

public class NbiPortFactory {

  private boolean vlanRequired = false;
  private String nmsPortId = UUID.randomUUID().toString();
  private String nmsNeId;
  private String nmsSapName;
  private InterfaceType interfaceType = InterfaceType.UNI;
  private String suggestedBodPortId = "suggestedId";
  private String suggestedNocLabel = "suggestedNocLabel";

  public NbiPort create() {
    NbiPort port = new NbiPort();
    port.setVlanRequired(vlanRequired);
    port.setNmsPortId(nmsPortId);
    port.setNmsSapName(nmsSapName);
    port.setNmsNeId(nmsNeId);
    port.setInterfaceType(interfaceType);
    port.setSuggestedBodPortId(suggestedBodPortId);
    port.setSuggestedNocLabel(suggestedNocLabel);

    return port;
  }

  public NbiPortFactory setInterfaceType(InterfaceType interfaceType) {
    this.interfaceType = interfaceType;
    return this;
  }

  public NbiPortFactory setVlanRequired(boolean vlanRequired) {
    this.vlanRequired = vlanRequired;
    return this;
  }

  public NbiPortFactory setNmsPortId(String nmsPortId) {
    this.nmsPortId = nmsPortId;
    return this;
  }

  public NbiPortFactory setNmsNeId(String nmsNeId) {
    this.nmsNeId = nmsNeId;
    return this;
  }

  public NbiPortFactory setNmsSapName(String nmsSapName) {
    this.nmsSapName = nmsSapName;
    return this;
  }

  public NbiPortFactory setSuggestedBodPortId(String suggestedBodPortId) {
    this.suggestedBodPortId = suggestedBodPortId;
    return this;
  }

  public NbiPortFactory setSuggestedNocLabel(String suggestedNocLabel) {
    this.suggestedNocLabel = suggestedNocLabel;
    return this;
  }

}

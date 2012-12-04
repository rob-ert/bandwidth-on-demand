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
package nl.surfnet.bod.support;

import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

public class ActivationEmailLinkFactory {

  private static final AtomicLong COUNTER = new AtomicLong();
  private Long id = COUNTER.incrementAndGet();

  private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();
  private boolean activate = false;
  private boolean emailSent = true;

  public ActivationEmailLink create() {
    ActivationEmailLink link = new ActivationEmailLink(physicalResourceGroup);
    link.setId(id);

    if (activate) {
      link.activate();
    }

    if (emailSent) {
      link.emailWasSent();
    }

    return link;
  }

  public ActivationEmailLinkFactory setActivate(boolean active) {
    this.activate = active;
    return this;
  }

  public ActivationEmailLinkFactory setEmailSent(boolean sent) {
    this.emailSent = sent;
    return this;
  }

  public ActivationEmailLinkFactory setPhysicalResourceGroup(PhysicalResourceGroup prg) {
    this.physicalResourceGroup = prg;
    return this;
  }

}

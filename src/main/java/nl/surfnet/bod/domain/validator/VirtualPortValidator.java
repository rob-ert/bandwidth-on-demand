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
package nl.surfnet.bod.domain.validator;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.web.security.Security;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for the {@link VirtualPort}. Validates that the
 * {@link VirtualPort#getManagerLabel()} is unique.
 *
 */
@Component
public class VirtualPortValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return VirtualPort.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object objToValidate, Errors errors) {
    VirtualPort virtualPort = (VirtualPort) objToValidate;

    validatePhysicalPort(virtualPort, errors);
    validateBandwidth(virtualPort, errors);
    validateVlanRequired(virtualPort, errors);
  }

  private void validatePhysicalPort(VirtualPort virtualPort, Errors errors) {
    PhysicalResourceGroup prg = virtualPort.getPhysicalResourceGroup();

    if (!Security.isManagerMemberOf(prg)) {
      errors.rejectValue("physicalPort", "validation.virtualport.physicalport.security",
          "You do not have the right permissions for this port");
    }
  }

  private void validateBandwidth(VirtualPort virtualPort, Errors errors) {
    if (virtualPort.getMaxBandwidth() != null && virtualPort.getMaxBandwidth() < 1) {
      errors.rejectValue("maxBandwidth", "validation.low");
    }
  }

  private void validateVlanRequired(VirtualPort virtualPort, Errors errors) {
    if ((virtualPort.getPhysicalPort() == null || virtualPort.getPhysicalPort().isVlanRequired())
        && ((virtualPort.getVlanId() == null) || Integer.valueOf(0).equals(virtualPort.getVlanId()))) {
      errors.rejectValue("vlanId", "validation.virtualport.vlanid.required.because.physicalport.requires.it");
    }

    if ((virtualPort.getVlanId() != null) && (virtualPort.getVlanId().intValue() > 0)
        && ((virtualPort.getPhysicalPort() == null || !virtualPort.getPhysicalPort().isVlanRequired()))) {
      errors.rejectValue("vlanId", "validation.virtualport.vlanid.not.allowed.since.physicalport.does.not.require.it");
    }
  }

}

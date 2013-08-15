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
package nl.surfnet.bod.domain.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.NbiPortFactory;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class VirtualPortValidatorTest {

  private VirtualPortValidator subject;

  private VirtualPort virtualPort;
  private PhysicalPort physicalPort;
  private PhysicalPort physicalPortVlanRequired;

  @Before
  public void initSecurity() {
    Security.setUserDetails(new RichUserDetailsFactory().addUserGroup("urn:my-group").addUserGroup("urn:test:group").create());

    subject = new VirtualPortValidator();
    physicalPort = new PhysicalPortFactory().create();
    virtualPort = new VirtualPortFactory().setPhysicalPort(physicalPort).setPhysicalPortAdminGroup("urn:my-group").create();
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().setAdminGroup("urn:my-group").create();
    physicalPortVlanRequired = new PhysicalPortFactory().setPhysicalResourceGroup(prg).setNbiPort(new NbiPortFactory().setVlanRequired(true).create()).create();
  }

  @Test
  public void testSupportsValidClass() {
    assertTrue(subject.supports(VirtualPort.class));
    assertFalse(subject.supports(Object.class));
  }

  @Test
  public void whenUserIsNotMemberOfAdminGroupShouldGiveAnError() {
    virtualPort.getPhysicalPort().setPhysicalResourceGroup(
        new PhysicalResourceGroupFactory().setAdminGroup("urn:other:group").create());
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);

    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("physicalPort"));
  }

  @Test
  public void negativeBandwidth() {
    virtualPort.setMaxBandwidth(-1L);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);

    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("maxBandwidth"));
  }

  @Test
  public void zeroBandwidth() {
    virtualPort.setMaxBandwidth(0L);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);

    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("maxBandwidth"));
  }

  @Test
  public void minimalBandwidth() {
    virtualPort.setMaxBandwidth(1L);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);

    assertFalse(errors.hasErrors());
  }

  @Test
  public void nullBandwidth() {
    virtualPort.setMaxBandwidth(null);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);

    // validation error is added by hibernate validator...
    assertFalse(errors.hasErrors());
  }

  @Test
  public void withoutAPhysicalPort() {
    virtualPort.setPhysicalPort(null);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);

    assertTrue(errors.hasErrors());
  }

  private Errors createErrorObject(VirtualPort port) {
    return new BeanPropertyBindingResult(port, "virtualPort");
  }

  @Test
  public void vlanIdShouldBeRequiredSincePhysicalPortRequiresIt() {
    virtualPort.setPhysicalPort(physicalPortVlanRequired);
    virtualPort.setVlanId(null);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);
    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("vlanId"));
  }

  @Test
  public void vlanIdShouldBeRequiredWithValueZeroSincePhysicalPortRequiresIt() {
    virtualPort.setPhysicalPort(physicalPortVlanRequired);
    virtualPort.setVlanId(0);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);
    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("vlanId"));
  }

  @Test
  public void vlanIdShouldBeRequiredWithValueOneSincePhysicalPortRequiresIt() {
    virtualPort.setPhysicalPort(physicalPortVlanRequired);
    virtualPort.setVlanId(1);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);
    assertFalse(errors.hasGlobalErrors());
    assertFalse(errors.hasErrors());
  }

  @Test
  public void vlanIdShouldNotBeRequiredSincePhysicalPortDoesNotRequireIt() {
    virtualPort.setVlanId(null);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);
    assertFalse(errors.hasGlobalErrors());
    assertFalse(errors.hasErrors());
  }

  @Test
  public void vlanIdMayNotBePresentSincePhysicalPortDoesNotRequireIt() {
    virtualPort.setVlanId(1);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);
    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("vlanId"));
  }

}

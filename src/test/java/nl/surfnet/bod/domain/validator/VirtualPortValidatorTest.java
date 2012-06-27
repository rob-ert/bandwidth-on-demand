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
package nl.surfnet.bod.domain.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.VirtualPort;
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
    Security.setUserDetails(new RichUserDetailsFactory().addUserGroup("urn:my-group").addUserGroup("urn:test:group")
        .create());

    subject = new VirtualPortValidator();
    physicalPort = new PhysicalPortFactory().create();
    physicalPortVlanRequired = new PhysicalPortFactory().setVlanRequired(true).create();
    virtualPort = new VirtualPortFactory().setPhysicalPort(physicalPort).create();
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
    virtualPort.setMaxBandwidth(-1);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);

    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("maxBandwidth"));
  }

  @Test
  public void zeroBandwidth() {
    virtualPort.setMaxBandwidth(0);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);

    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("maxBandwidth"));
  }

  @Test
  public void minimalBandwidth() {
    virtualPort.setMaxBandwidth(1);
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

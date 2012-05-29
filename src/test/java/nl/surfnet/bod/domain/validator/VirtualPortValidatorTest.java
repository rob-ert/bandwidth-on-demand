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
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortValidatorTest {

  @InjectMocks
  private VirtualPortValidator subject;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  private VirtualPort virtualPort;

  private PhysicalPort physicalPort;

  @Before
  public void initSecurity() {
    Security.setUserDetails(new RichUserDetailsFactory().addUserGroup("urn:mygroup").addUserGroup("urn:test:group")
        .create());

    physicalPort = new PhysicalPortFactory().create();
    virtualPort = new VirtualPortFactory().setPhysicalPort(physicalPort).create();
  }

  @Test
  public void testSupportsValidClass() {
    assertTrue(subject.supports(VirtualPort.class));
    assertFalse(subject.supports(Object.class));
  }

  @Test
  public void testExistingName() {
    VirtualPort existingPort = new VirtualPortFactory().setManagerLabel("one").create();
    VirtualPort newPort = new VirtualPortFactory().setId(null).setManagerLabel("one").create();

    when(virtualPortServiceMock.findByManagerLabel("one")).thenReturn(existingPort);
    Errors errors = createErrorObject(newPort);

    subject.validate(newPort, errors);

    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("managerLabel"));
  }

  @Test
  public void testNonExistingName() {
    virtualPort.setManagerLabel("one");

    when(virtualPortServiceMock.findByManagerLabel("one")).thenReturn(null);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);

    assertFalse(errors.hasErrors());
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
    VirtualPort port = new VirtualPortFactory().setMaxBandwidth(0).setPhysicalPortAdminGroup("urn:mygroup").create();
    Errors errors = createErrorObject(port);

    subject.validate(port, errors);

    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("maxBandwidth"));
  }

  @Test
  public void minimalBandwidth() {
    VirtualPort port = new VirtualPortFactory().setMaxBandwidth(1).setPhysicalPortAdminGroup("urn:mygroup").create();
    Errors errors = createErrorObject(port);

    subject.validate(port, errors);

    assertFalse(errors.hasErrors());
  }

  @Test
  public void nullBandwidth() {
    VirtualPort port = new VirtualPortFactory().setMaxBandwidth(null).setPhysicalPortAdminGroup("urn:mygroup").create();
    Errors errors = createErrorObject(port);

    subject.validate(port, errors);

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
    physicalPort.setVlanRequired(true);
    virtualPort.setVlanId(null);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);
    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("vlanId"));
  }

  @Test
  public void vlanIdShouldBeRequiredWithValueZeroSincePhysicalPortRequiresIt() {
    physicalPort.setVlanRequired(true);
    virtualPort.setVlanId(0);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);
    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("vlanId"));
  }

  @Test
  public void vlanIdShouldBeRequiredWithValueOneSincePhysicalPortRequiresIt() {
    physicalPort.setVlanRequired(true);
    virtualPort.setVlanId(1);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);
    assertFalse(errors.hasGlobalErrors());
    assertFalse(errors.hasErrors());
  }

  @Test
  public void vlanIdShouldNotBeRequiredSincePhysicalPortDoesNotRequireIt() {
    physicalPort.setVlanRequired(false);
    virtualPort.setVlanId(null);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);
    assertFalse(errors.hasGlobalErrors());
    assertFalse(errors.hasErrors());
  }

  @Test
  public void vlanIdMayNotBePresentSincePhysicalPortDoesNotRequireIt() {
    physicalPort.setVlanRequired(false);
    virtualPort.setVlanId(1);
    Errors errors = createErrorObject(virtualPort);

    subject.validate(virtualPort, errors);
    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("vlanId"));
  }

}

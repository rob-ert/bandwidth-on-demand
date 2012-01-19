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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class PhysicalResourceGroupValidatorTest {

  private PhysicalResourceGroupService physicalResourceGroupServiceMock;
  private PhysicalResourceGroupValidator subject;

  @Before
  public void initController() {
    subject = new PhysicalResourceGroupValidator();
    physicalResourceGroupServiceMock = mock(PhysicalResourceGroupService.class);
    subject.setPhysicalResourceGroupService(physicalResourceGroupServiceMock);
  }

  @Test
  public void testSupportsValidClass() {
    assertTrue(subject.supports(PhysicalResourceGroup.class));
  }

  @Test
  public void testSupportsInValidClass() {
    assertFalse(subject.supports(Object.class));
  }

  @Test
  public void testValidateExistingNameUpdate() {
    PhysicalResourceGroup physicalResourceGroupOne = new PhysicalResourceGroupFactory().setInstituteId(1L).create();

    when(physicalResourceGroupServiceMock.findByInstituteId(1L)).thenReturn(physicalResourceGroupOne);

    Errors errors = new BeanPropertyBindingResult(physicalResourceGroupOne, "physicalResourceGroup");
    assertFalse(errors.hasErrors());

    subject.validate(physicalResourceGroupOne, errors);

    assertFalse(errors.hasErrors());
  }

  @Test
  public void testValidateExistingNameInsert() {
    PhysicalResourceGroup physicalResourceGroupOne = new PhysicalResourceGroupFactory().setId(1l).setInstituteId(1L)
        .create();
    PhysicalResourceGroup physicalResourceGroupTwo = new PhysicalResourceGroupFactory().setId(2L).setInstituteId(1L)
        .create();

    when(physicalResourceGroupServiceMock.findByInstituteId(1L)).thenReturn(physicalResourceGroupTwo);

    Errors errors = new BeanPropertyBindingResult(physicalResourceGroupOne, "physicalResourceGroup");
    assertFalse(errors.hasErrors());

    subject.validate(physicalResourceGroupOne, errors);

    assertTrue(errors.hasFieldErrors("name"));
  }

  @Test
  public void testValidateNonExistingName() {
    PhysicalResourceGroup physicalResourceGroupOne = new PhysicalResourceGroupFactory().setInstituteId(1L).create();

    when(physicalResourceGroupServiceMock.findByInstituteId(1L)).thenReturn(null);

    Errors errors = new BeanPropertyBindingResult(physicalResourceGroupOne, "physicalResourceGroup");
    assertFalse(errors.hasErrors());

    subject.validate(physicalResourceGroupOne, errors);

    assertFalse(errors.hasErrors());
  }

}

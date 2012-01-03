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
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class VirtualPortValidatorTest {

  private VirtualPortService virtualPortServiceMock;
  private VirtualPortValidator subject;

  @Before
  public void initController() {
    subject = new VirtualPortValidator();
    virtualPortServiceMock = mock(VirtualPortService.class);
    subject.setVirtualPortService(virtualPortServiceMock);
  }

  @Test
  public void testSupportsValidClass() {
    assertTrue(subject.supports(VirtualPort.class));
  }

  @Test
  public void testSupportsInValidClass() {
    assertFalse(subject.supports(Object.class));
  }

  @Test
  public void testExistingName() {
    VirtualPort existingPort = new VirtualPortFactory().setName("one").create();
    VirtualPort newPort = new VirtualPortFactory().setId(null).setName("one").create();

    when(virtualPortServiceMock.findByName("one")).thenReturn(existingPort);

    Errors errors = new BeanPropertyBindingResult(newPort, "virtualPort");

    subject.validate(newPort, errors);

    assertFalse(errors.hasGlobalErrors());
    assertTrue(errors.hasFieldErrors("name"));
  }

  @Test
  public void testNonExistingName() {
    VirtualPort virtualPortOne = new VirtualPortFactory().setName("one").create();

    when(virtualPortServiceMock.findByName("one")).thenReturn(null);

    Errors errors = new BeanPropertyBindingResult(virtualPortOne, "virtualPort");

    assertFalse(errors.hasErrors());

    subject.validate(virtualPortOne, errors);

    assertFalse(errors.hasErrors());
  }

}

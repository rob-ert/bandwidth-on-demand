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

import org.junit.Test;

public class ValidatorHelperTest {

  private ValidatorHelper subject = new ValidatorHelper();

  // Create
  // First argument does not matter, since there is no id yet
  @Test
  public void testCreateAlreadyExistingName() {
    assertFalse(subject.validateNameUniqueness(true, true, false));
  }

  @Test
  public void testMatchingIdsAndNewName() {
    assertTrue(subject.validateNameUniqueness(true, false, false));
  }

  @Test
  public void testCreateToAlreadyExistingName() {
    assertFalse(subject.validateNameUniqueness(false, true, false));
  }

  @Test
  public void testCreateNonExistingName() {
    assertTrue(subject.validateNameUniqueness(false, false, false));
  }

  // Update
  @Test
  public void testUpdateSameObject() {
    assertTrue(subject.validateNameUniqueness(true, true, true));
  }

  @Test
  public void testChangeNameSamedObject() {
    assertTrue(subject.validateNameUniqueness(true, false, true));
  }

  @Test
  public void testChangNameToDifferentExistingObject() {
    assertFalse(subject.validateNameUniqueness(false, true, true));
  }

  @Test
  public void testChangeNameToNonExisting() {
    assertTrue(subject.validateNameUniqueness(false, false, true));
  }

}

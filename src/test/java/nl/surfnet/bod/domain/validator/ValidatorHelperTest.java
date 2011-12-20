package nl.surfnet.bod.domain.validator;

import static org.junit.Assert.*;
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

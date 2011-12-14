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
  public void testValidateExistingName() {
    PhysicalResourceGroup physicalResourceGroupOne = new PhysicalResourceGroupFactory().setName("one").create();

    when(physicalResourceGroupServiceMock.findByName("one")).thenReturn(physicalResourceGroupOne);

    Errors errors = new BeanPropertyBindingResult(physicalResourceGroupOne, "physicalResourceGroup");
    assertFalse(errors.hasErrors());
    assertFalse(errors.hasFieldErrors());
    assertFalse(errors.hasGlobalErrors());

    subject.validate(physicalResourceGroupOne, errors);

    assertTrue(errors.hasFieldErrors("name"));
    assertFalse(errors.hasGlobalErrors());
  }
  
  
  @Test
  public void testValidateNonExistingName() {
    PhysicalResourceGroup physicalResourceGroupOne = new PhysicalResourceGroupFactory().setName("one").create();

    when(physicalResourceGroupServiceMock.findByName("one")).thenReturn(null);

    Errors errors = new BeanPropertyBindingResult(physicalResourceGroupOne, "physicalResourceGroup");
    assertFalse(errors.hasErrors());
    assertFalse(errors.hasFieldErrors());
    assertFalse(errors.hasGlobalErrors());

    subject.validate(physicalResourceGroupOne, errors);

    assertFalse(errors.hasFieldErrors());
    assertFalse(errors.hasGlobalErrors());
  }

}

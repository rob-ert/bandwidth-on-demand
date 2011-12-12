package nl.surfnet.bod.domain.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class VirtualResourceGroupValidatorTest {

  private VirtualResourceGroupService virtualResourceGroupServiceMock;
  private VirtualResourceGroupValidator subject;

  @Before
  public void initController() {
    subject = new VirtualResourceGroupValidator();
    virtualResourceGroupServiceMock = mock(VirtualResourceGroupService.class);
    subject.setVirtualResourceGroupService(virtualResourceGroupServiceMock);
  }

  @Test
  public void testSupportsValidClass() {
    assertTrue(subject.supports(VirtualResourceGroup.class));
  }

  @Test
  public void testSupportsInValidClass() {
    assertFalse(subject.supports(Object.class));
  }

  @Test
  public void testValidateExistingInstance() {
    VirtualResourceGroup virtualResourceGroupOne = new VirtualResourceGroupFactory().setSurfConnextGroupName("one")
        .create();

    when(virtualResourceGroupServiceMock.findBySurfConnextGroupName("one")).thenReturn(virtualResourceGroupOne);
    Errors errors = new BeanPropertyBindingResult(virtualResourceGroupOne, "virtualResourceGroup");
    assertFalse(errors.hasErrors());
    assertFalse(errors.hasFieldErrors());
    assertFalse(errors.hasGlobalErrors());

    subject.validate(virtualResourceGroupOne, errors);

    assertTrue(errors.hasFieldErrors("surfConnextGroupName"));
    assertFalse(errors.hasGlobalErrors());
  }
  
  
  @Test
  public void testValidateNonExistingInstance() {
    VirtualResourceGroup virtualResourceGroupOne = new VirtualResourceGroupFactory().setSurfConnextGroupName("one")
        .create();

    when(virtualResourceGroupServiceMock.findBySurfConnextGroupName("one")).thenReturn(null);
    Errors errors = new BeanPropertyBindingResult(virtualResourceGroupOne, "virtualResourceGroup");
    assertFalse(errors.hasErrors());
    assertFalse(errors.hasFieldErrors());
    assertFalse(errors.hasGlobalErrors());

    subject.validate(virtualResourceGroupOne, errors);

    assertFalse(errors.hasFieldErrors("surfConnextGroupName"));
    assertFalse(errors.hasGlobalErrors());
  }


}

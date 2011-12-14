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
    VirtualPort virtualPortOne = new VirtualPortFactory().setName("one").create();

    when(virtualPortServiceMock.findByName("one")).thenReturn(virtualPortOne);

    Errors errors = new BeanPropertyBindingResult(virtualPortOne, "virtualPort");

    assertFalse(errors.hasErrors());

    subject.validate(virtualPortOne, errors);

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

package nl.surfnet.bod.domain.validator;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.VirtualPortService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for the {@link VirtualPort}. Validates that the
 * {@link VirtualPort#getName()} is unique.
 * 
 * @author Franky
 * 
 */
@Component
public class VirtualPortValidator implements Validator {

  @Autowired
  private VirtualPortService virtualPortService;

  @Override
  public boolean supports(Class<?> clazz) {
    return VirtualPort.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object objToValidate, Errors errors) {
    VirtualPort virtualPort= (VirtualPort) objToValidate;

    if (virtualPortService.findByName(virtualPort.getName()) != null) {
      // An instance already exists
      errors.rejectValue("name", "validation.not.unique");
    }

  }

  public void setVirtualPortService(VirtualPortService virtualPortService) {
    this.virtualPortService = virtualPortService;
  }
}

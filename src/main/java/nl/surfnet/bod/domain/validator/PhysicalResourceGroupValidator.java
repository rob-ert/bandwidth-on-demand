package nl.surfnet.bod.domain.validator;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for the {@link PhysicalResourceGroup}. Validates that the
 * {@link PhysicalResourceGroup#getName()} is unique.
 * 
 * @author Franky
 * 
 */
@Component
public class PhysicalResourceGroupValidator implements Validator {

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Override
  public boolean supports(Class<?> clazz) {
    return PhysicalResourceGroup.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object objToValidate, Errors errors) {
    PhysicalResourceGroup physicalResourceGroup = (PhysicalResourceGroup) objToValidate;

    if (physicalResourceGroupService.findByName(physicalResourceGroup.getName()) != null) {
      // An instance already exists
      errors.rejectValue("name", "validation.not.unique");
    }

  }

  public void setPhysicalResourceGroupService(PhysicalResourceGroupService physicalResourceGroupService) {
    this.physicalResourceGroupService = physicalResourceGroupService;
  }
}

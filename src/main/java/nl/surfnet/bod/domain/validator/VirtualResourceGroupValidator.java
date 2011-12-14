package nl.surfnet.bod.domain.validator;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.VirtualResourceGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for the {@link VirtualResourceGroup}. Validates that the
 * {@link VirtualResourceGroup#getSurfConnextGroupName()} is unique and exists
 * in SurfConext.
 * 
 * @author Franky
 * 
 */
@Component
public class VirtualResourceGroupValidator implements Validator {

  @Autowired
  private VirtualResourceGroupService virtualResourceGroupService;

  @Override
  public boolean supports(Class<?> clazz) {
    return VirtualResourceGroup.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object objToValidate, Errors errors) {
    VirtualResourceGroup virtualResourceGroup = (VirtualResourceGroup) objToValidate;

    // Check the surfConnextGroupName
    if (virtualResourceGroupService.findBySurfConnextGroupName(virtualResourceGroup.getSurfConnextGroupName()) != null) {
      // An instance already exists
      errors.rejectValue("surfConnextGroupName", "validation.not.unique");
    }

    // TODO Check if the surfConextGroupName exists in SurfConext

    // Check the name
    if (virtualResourceGroupService.findByName(virtualResourceGroup.getName()) != null) {
      // An instance already exists
      errors.rejectValue("name", "validation.not.unique");
    }

  }

  void setVirtualResourceGroupService(VirtualResourceGroupService virtualResourceGroupService) {
    this.virtualResourceGroupService = virtualResourceGroupService;
  }
}

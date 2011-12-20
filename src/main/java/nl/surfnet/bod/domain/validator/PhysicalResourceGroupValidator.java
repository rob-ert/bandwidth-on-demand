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

  private ValidatorHelper validatorHelper = new ValidatorHelper();

  @Override
  public boolean supports(Class<?> clazz) {
    return PhysicalResourceGroup.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object objToValidate, Errors errors) {
    PhysicalResourceGroup physicalResourceGroup = (PhysicalResourceGroup) objToValidate;

    PhysicalResourceGroup existingResourceGroup = physicalResourceGroupService.findByName(physicalResourceGroup
        .getName());

    if (existingResourceGroup != null) {
      if (!validatorHelper.validateNameUniqueness(physicalResourceGroup.getId() == existingResourceGroup.getId(),
          physicalResourceGroup.getName().equalsIgnoreCase(existingResourceGroup.getName()),
          physicalResourceGroup.getId() != null)) {
        errors.rejectValue("name", "validation.not.unique");
      }
    }
  }

  public void setPhysicalResourceGroupService(PhysicalResourceGroupService physicalResourceGroupService) {
    this.physicalResourceGroupService = physicalResourceGroupService;
  }
}

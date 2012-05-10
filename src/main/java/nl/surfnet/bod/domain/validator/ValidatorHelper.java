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

/**
 * Placeholder for common validation logic, since spring data repositories can
 * not handle generics correctly.
 * 
 * @author Franky
 * 
 */
public class ValidatorHelper {

  /**
   * Validates the uniqueness of a name.
   * 
   * @param userInputName
   *          String Name which was entered by the user
   * @param nameAlreadyExists
   *          boolean true is name already exists in database, false otherwise
   * @param isUpdate
   *          boolean true means update, false create
   * @return true is the name is valid, false otherwise
   */
  public boolean validateNameUniqueness(boolean matchingIds, boolean matchingNames, boolean isUpdate) {
    boolean valid = false;

    if (isUpdate) {
      // Update
      valid = !(matchingNames && !matchingIds);
    } else {
      // Create
      valid = !matchingNames;
    }

    return valid;
  }
}

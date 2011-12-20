package nl.surfnet.bod.domain.validator;


/**
 * Placeholder for common valiation logic, since spring data repositories can
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
      valid = (!(matchingNames && !matchingIds));
    }
    else {
      // Create
      valid = (!matchingNames);
    }
    return valid;
  }
}

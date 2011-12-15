package nl.surfnet.bod.web;

import static nl.surfnet.bod.web.WebUtils.*;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.domain.validator.ReservationValidator;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.util.UserContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@SessionAttributes({ "userContext" })
@RequestMapping(ReservationController.PAGE_URL_PREFIX + ReservationController.PAGE_URL)
@Controller
public class ReservationController {
  public static final String PAGE_URL_PREFIX = "/manager/";
  static final String PAGE_URL = "reservations";

  static final String MODEL_KEY = "reservation";
  static final String MODEL_KEY_LIST = MODEL_KEY + LIST_POSTFIX;

  @Autowired
  private ReservationService reservationService;

  @Autowired
  private VirtualResourceGroupService virtualResourceGroupService;
  
  private ReservationValidator reservationValidator = new ReservationValidator();

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid Reservation reservation, final BindingResult bindingResult, final Model uiModel,
      final HttpServletRequest httpServletRequest) {

    reservationValidator.validate(reservation, bindingResult);
    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(MODEL_KEY, reservation);
      return PAGE_URL + CREATE;
    }

    uiModel.asMap().clear();
    reservationService.save(reservation);

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, new Reservation());

    return PAGE_URL + CREATE;
  }

  @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
  public String show(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, reservationService.find(id));
    // Needed for the default icons
    uiModel.addAttribute(ICON_ITEM_KEY, id);

    return PAGE_URL + SHOW;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY_LIST, reservationService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(reservationService.count()));

    return PAGE_URL + LIST;
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid final Reservation reservation, final BindingResult bindingResult, final Model uiModel,
      final HttpServletRequest httpServletRequest) {

    reservationValidator.validate(reservation, bindingResult);
    if (bindingResult.hasErrors()) {
      uiModel.addAttribute(MODEL_KEY, reservation);
      return PAGE_URL + UPDATE;
    }

    uiModel.asMap().clear();
    reservationService.update(reservation);

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
  public String updateForm(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    uiModel.addAttribute(MODEL_KEY, reservationService.find(id));
    return PAGE_URL + UPDATE;
  }

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(@RequestParam(ID_KEY) final Long id,
      @RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {

    Reservation reservation = reservationService.find(id);
    reservationService.delete(reservation);

    uiModel.asMap().clear();
    uiModel.addAttribute(PAGE_KEY, (page == null) ? "1" : page.toString());

    return "redirect:";
  }

  /**
   * Puts all {@link VirtualResourceGroup}s related to the user on the model
   * 
   * @param userContext
   *          UserContext
   * @return Collection<VirtualResourceGroup> or empty Collection when no match
   *         found.
   */
  @ModelAttribute(VirtualResourceGroupController.MODEL_KEY_LIST)
  public Collection<VirtualResourceGroup> populateVirtualResourceGroups(@ModelAttribute UserContext userContext) {

    return virtualResourceGroupService.findAllForUser(userContext.getNameId());

  }

  /**
   * Setter to enable depedency injection from testcases.
   * 
   * @param virtualPortService
   *          {@link VirtualPortService}
   */
  protected void setVirtualResourceGroupService(VirtualResourceGroupService virtualResourceGroupService) {
    this.virtualResourceGroupService = virtualResourceGroupService;
  }
}
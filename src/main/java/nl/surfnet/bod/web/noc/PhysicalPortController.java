/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.noc;

import static nl.surfnet.bod.web.WebUtils.ID_KEY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Resource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.service.NocService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.base.MessageManager;
import nl.surfnet.bod.web.push.EndPoints;
import nl.surfnet.bod.web.view.ElementActionView;
import nl.surfnet.bod.web.view.ReservationView;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/noc/physicalports")
public class PhysicalPortController {

  @Resource private PhysicalPortService physicalPortService;
  @Resource private PhysicalResourceGroupService physicalResourceGroupService;
  @Resource private VirtualPortService virtualPortService;
  @Resource private ReservationService reservationService;
  @Resource private NocService nocService;
  @Resource private MessageManager messageManager;
  @Resource private EndPoints endPoints;

  public static boolean containsLetters(String input) {
    if (input == null || input.length() <= 0) {
      return false;
    }
    for (int i = 0; i < input.length(); i++) {
      if (Character.isLetter(input.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  @VisibleForTesting
  void setMessageManager(MessageManager messageManager) {
    this.messageManager = messageManager;
  }

//  private void sortExternalResources(String sort, String order, Model model, List<PhysicalPortView> transformedUnallocatedPhysicalPorts) {
//    prepareSortOptions(sort, order, model);
//    Collections.sort(transformedUnallocatedPhysicalPorts, new ReflectiveFieldComparator(sort));
//
//    if (StringUtils.hasText(order) && "DESC".equals(order)) {
//      Collections.reverse(transformedUnallocatedPhysicalPorts);
//    }
//  }

  @RequestMapping(value = "create", params = ID_KEY, method = RequestMethod.GET)
  public String createForm(@RequestParam(ID_KEY) String nmsPortId, Model model) {
    Optional<NbiPort> nbiPort = physicalPortService.findNbiPort(nmsPortId);

    if (!nbiPort.isPresent()) {
      return "redirect:";
    }

    PhysicalPort physicalPort = PhysicalPort.create(nbiPort.get());

    if (physicalPort instanceof UniPort) {
      model.addAttribute("createUniPortCommand", new UniPortController.CreateUniPortCommand((UniPort) physicalPort));

      return "physicalports/uni/create";
    } else {
      model.addAttribute("createEnniPortCommand", new EnniPortController.CreateEnniPortCommand((EnniPort) physicalPort));
      model.addAttribute("vlanRequired", physicalPort.isVlanRequired());

      return "physicalports/enni/create";
    }
  }

  @RequestMapping(value = "move", method = RequestMethod.GET)
  public String moveForm(@RequestParam Long id, Model model, RedirectAttributes redirectAttrs) {
    final PhysicalPort port = physicalPortService.find(id);

    if (port == null) {
      redirectAttrs.addFlashAttribute(MessageManager.INFO_MESSAGES_KEY, ImmutableList.of("Could not find port.."));
      return "redirect:/noc/physicalports";
    }

    Collection<NbiPort> unallocatedPorts = Collections2.filter(physicalPortService.findUnallocated(), new Predicate<NbiPort>() {
      @Override
      public boolean apply(NbiPort input) {
        return input.isVlanRequired() == port.isVlanRequired();
      }
    });

    if (unallocatedPorts.isEmpty()) {
      messageManager.addInfoFlashMessage(redirectAttrs, "info_physicalport_nounallocated", port.isVlanRequired() ? "EVPL" : "EPL");
      return "redirect:/noc/physicalports";
    }

    long numberOfVirtualPorts = 0;
    if (port instanceof UniPort) {
      numberOfVirtualPorts = virtualPortService.countForUniPort((UniPort) port);
    }

    long numberOfReservations = reservationService.countForPhysicalPort(port);
    long numberOfActiveReservations = reservationService.countActiveReservationsForPhysicalPort(port);

    model.addAttribute("relatedObjects", new RelatedObjects(numberOfVirtualPorts, numberOfReservations, numberOfActiveReservations));
    model.addAttribute("physicalPort", port);
    model.addAttribute("unallocatedPhysicalPorts", unallocatedPorts);
    model.addAttribute("movePhysicalPortCommand", new MovePhysicalPortCommand(port));

    return "physicalports/move";
  }

  public static final class RelatedObjects {
    private final Long numberOfVirtualPorts;
    private final Long numberOfReservations;
    private final Long numberOfActiveReservations;

    public RelatedObjects(Long numberOfVirtualPorts, Long numberOfReservations, Long numberOfActiveReservations) {
      this.numberOfActiveReservations = numberOfActiveReservations;
      this.numberOfVirtualPorts = numberOfVirtualPorts;
      this.numberOfReservations = numberOfReservations;
    }

    public Long getNumberOfVirtualPorts() {
      return numberOfVirtualPorts;
    }

    public Long getNumberOfReservations() {
      return numberOfReservations;
    }

    public Long getNumberOfActiveReservations() {
      return numberOfActiveReservations;
    }
  }

  @RequestMapping(value = "move", method = RequestMethod.PUT)
  public String move(MovePhysicalPortCommand command, BindingResult result, Model model) {

    Optional<NbiPort> newPort = physicalPortService.findNbiPort(command.getNewPhysicalPort());
    if (!newPort.isPresent()) {
      return "redirect:";
    }
    UniPort oldPort = physicalPortService.findUniPort(command.getId());

    model.addAttribute("lastEventId", endPoints.getLastEventId());
    Collection<Reservation> reservations = nocService.movePort(oldPort, newPort.get());

    List<ReservationView> reservationViews = new ArrayList<>();
    for (Reservation reservation : reservations) {
      ReservationView reservationView = new ReservationView(reservation, new ElementActionView(false), new ElementActionView(false));
      reservationViews.add(reservationView);
    }
    model.addAttribute("list", reservationViews);

    return "physicalports/moveResult";
  }

  public static class MovePhysicalPortCommand {
    private Long id;
    private String newPhysicalPort;

    public MovePhysicalPortCommand() {
    }

    public MovePhysicalPortCommand(PhysicalPort port) {
      this.id = port.getId();
    }

    public String getNewPhysicalPort() {
      return newPhysicalPort;
    }

    public void setNewPhysicalPort(String newPhysicalPort) {
      this.newPhysicalPort = newPhysicalPort;
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }
  }

  public static class PhysicalPortCommand {
    @NotEmpty private String nmsPortId;
    @NotEmpty private String nocLabel;
    @NotEmpty private String bodPortId;

    public PhysicalPortCommand() {
    }

    public String getNmsPortId() {
      return nmsPortId;
    }

    public void setNmsPortId(String nmsPortId) {
      this.nmsPortId = nmsPortId;
    }

    public String getNocLabel() {
      return nocLabel;
    }

    public void setNocLabel(String nocLabel) {
      this.nocLabel = nocLabel;
    }

    public String getBodPortId() {
      return bodPortId;
    }

    public void setBodPortId(String portId) {
      this.bodPortId = portId;
    }

  }

  public enum PhysicalPortFilter {
    UNI_ALLOCATED("UNI", "/uni"),
    ENNI_ALLOCATED("E-NNI", "/enni"),
    UN_ALLOCATED("Unallocated", "/free"),
    UN_ALIGNED("Unaligned", "/unaligned");

    private final String path;
    private final String name;

    public static Collection<PhysicalPortFilter> getAvailableFilters() {
      return EnumSet.allOf(PhysicalPortFilter.class);
    }

    private PhysicalPortFilter(String name, String path) {
      this.name = name;
      this.path = path;
    }

    public String getPath() {
      return path;
    }

    public String getName() {
      return name;
    }
  }

}
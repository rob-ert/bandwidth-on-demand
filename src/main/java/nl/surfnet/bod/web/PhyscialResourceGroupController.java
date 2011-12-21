package nl.surfnet.bod.web;

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.service.PhysicalResourceGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/physicalresourcegroups")
@Controller
public class PhyscialResourceGroupController {

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @RequestMapping(value = "/{id}/ports", method = RequestMethod.GET)
  public @ResponseBody Collection<PhysicalPort> listForPhysicalResourceGroup(@PathVariable Long id) {
    return physicalResourceGroupService.find(id).getPhysicalPorts();
  }
}

package nl.surfnet.bod.web;

import java.util.Collection;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.VirtualResourceGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/virtualresourcegroup")
public class VirtualResourceGroupController {

  @Autowired
  private VirtualResourceGroupService virtualResourceGroupService;

  @RequestMapping(value = "/{name}/ports", method = RequestMethod.GET)
  public @ResponseBody Collection<VirtualPort> listForVirtualResourceGroup(@PathVariable String name) {
    return virtualResourceGroupService.findByName(name).getVirtualPorts();
  }
}

package nl.surfnet.bod.web;

import nl.surfnet.bod.domain.PhysicalResourceGroup;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/physicalresourcegroups")
@Controller
@RooWebScaffold(path = "physicalresourcegroups", formBackingObject = PhysicalResourceGroup.class)
public class PhysicalResourceGroupController {
}

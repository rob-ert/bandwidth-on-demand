package org.surfnet.bod.web;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.surfnet.bod.domain.PhysicalResourceGroup;

@RequestMapping("/physicalresourcegroups")
@Controller
@RooWebScaffold(path = "physicalresourcegroups", formBackingObject = PhysicalResourceGroup.class)
public class PhysicalResourceGroupController {
}

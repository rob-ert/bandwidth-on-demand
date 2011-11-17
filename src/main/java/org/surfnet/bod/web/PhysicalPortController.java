package org.surfnet.bod.web;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.surfnet.bod.domain.PhysicalPort;

@RequestMapping("/physicalports")
@Controller
@RooWebScaffold(path = "physicalports", formBackingObject = PhysicalPort.class)
public class PhysicalPortController {
}

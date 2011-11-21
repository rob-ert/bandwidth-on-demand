package nl.surfnet.bod.web;

import nl.surfnet.bod.domain.PhysicalPort;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/physicalports")
@Controller
@RooWebScaffold(path = "physicalports", formBackingObject = PhysicalPort.class)
public class PhysicalPortController {
}

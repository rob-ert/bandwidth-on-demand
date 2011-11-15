package org.surfnet.bod.web;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.surfnet.bod.institution.Institution;

@RequestMapping("/institutions")
@Controller
@RooWebScaffold(path = "institutions", formBackingObject = Institution.class)
public class InstitutionController {
}

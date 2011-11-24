package nl.surfnet.bod.web;

import java.util.Collection;

import nl.surfnet.bod.domain.Institution;
import nl.surfnet.bod.service.InstitutionStaticService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class InstitutionController {

    @Autowired
    private InstitutionStaticService institutionStaticService;

    @RequestMapping(value = "/institutions", method = RequestMethod.GET)
    public String list(final Model uiModel) {
        Collection<Institution> institutions = institutionStaticService.getInstitutions();

        uiModel.addAttribute("institutions", institutions);

        return "institutions/list";
    }

}

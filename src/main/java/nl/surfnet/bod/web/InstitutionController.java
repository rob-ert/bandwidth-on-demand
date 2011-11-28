package nl.surfnet.bod.web;

import java.util.Collection;

import nl.surfnet.bod.domain.Institution;
import nl.surfnet.bod.service.InstitutionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

@Controller
public class InstitutionController {

    private InstitutionService institutionService;

    @Autowired
    public InstitutionController(@Qualifier("institutionStaticService") InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @RequestMapping(value = "/institutions", method = RequestMethod.GET, headers = "accept=application/json")
    public @ResponseBody
    Collection<Institution> jsonList(@RequestParam(required = false) String q) {
        Collection<Institution> institutions = institutionService.getInstitutions();

        final String query = q.toLowerCase();
        return Collections2.filter(institutions, new Predicate<Institution>() {
            @Override
            public boolean apply(Institution input) {
                return input.getName().toLowerCase().contains(query);
            }
        });
    }

    @RequestMapping(value = "/institutions", method = RequestMethod.GET)
    public String list(final Model uiModel) {
        Collection<Institution> institutions = institutionService.getInstitutions();

        uiModel.addAttribute("institutions", institutions);

        return "institutions/list";
    }

}

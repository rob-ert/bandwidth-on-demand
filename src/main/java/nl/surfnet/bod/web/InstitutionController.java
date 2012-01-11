/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.web;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static nl.surfnet.bod.web.WebUtils.LIST_POSTFIX;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.service.InstitutionService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;

@RequestMapping("/institutions")
@Controller
public class InstitutionController {

  static final String MODEL_KEY = "institution";
  static final String MODEL_KEY_LIST = MODEL_KEY + LIST_POSTFIX;

  private InstitutionService institutionService;

  private PhysicalResourceGroupService physicalResourceGroupService;

  @Autowired
  public InstitutionController(InstitutionService institutionService,
      PhysicalResourceGroupService physicalResourceGroupService) {
    this.institutionService = institutionService;
    this.physicalResourceGroupService = physicalResourceGroupService;
  }

  @RequestMapping(method = RequestMethod.GET, headers = "accept=application/json")
  public @ResponseBody
  Collection<Institute> jsonList(@RequestParam(required = false) String q) {
    final Collection<String> existingInstitutions = getExistingInstitutionNames();
    final String query = StringUtils.hasText(q) ? q.toLowerCase() : "";

    return filter(institutionService.getInstitutions(), new Predicate<Institute>() {
      @Override
      public boolean apply(Institute input) {
        String institutionName = (input == null ? null : input.getName());
        if (!Strings.isNullOrEmpty(institutionName)) {
          institutionName = institutionName.toLowerCase();
        }

        return !existingInstitutions.contains(institutionName) && institutionName.contains(query);
      }
    });
  }

  private Collection<String> getExistingInstitutionNames() {
    List<PhysicalResourceGroup> groups = physicalResourceGroupService.findAll();

    return newArrayList(transform(groups, new Function<PhysicalResourceGroup, String>() {
      @Override
      public String apply(PhysicalResourceGroup input) {
        Institute institute = physicalResourceGroupService.findInstituteByPhysicalResourceGroup(input);
        String instituteName = (institute == null ? null : institute.getName());

        if (!Strings.isNullOrEmpty(instituteName)) {
          instituteName = instituteName.toLowerCase();
        }

        return instituteName;
      }
    }));
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(final Model uiModel) {
    Collection<Institute> institutions = institutionService.getInstitutions();

    uiModel.addAttribute(MODEL_KEY_LIST, institutions);

    return "institutions/list";
  }

}

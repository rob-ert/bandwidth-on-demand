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
import nl.surfnet.bod.service.InstituteService;
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

@RequestMapping("/institutes")
@Controller
public class InstituteController {

  static final String MODEL_KEY = "institute";
  static final String MODEL_KEY_LIST = MODEL_KEY + LIST_POSTFIX;

  private InstituteService instituteService;

  private PhysicalResourceGroupService physicalResourceGroupService;

  @Autowired
  public InstituteController(InstituteService instituteService,
      PhysicalResourceGroupService physicalResourceGroupService) {
    this.instituteService = instituteService;
    this.physicalResourceGroupService = physicalResourceGroupService;
  }

  @RequestMapping(method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public Collection<Institute> jsonList(@RequestParam(required = false) String q) {
    final Collection<String> existingInstitutes = getExistingInstituteNames();
    final String query = StringUtils.hasText(q) ? q.toLowerCase() : "";

    return filter(instituteService.getInstitutes(), new Predicate<Institute>() {
      @Override
      public boolean apply(Institute input) {
        String instituteName = (input == null ? null : input.getName());
        if (!Strings.isNullOrEmpty(instituteName)) {
          instituteName = instituteName.toLowerCase();
        }

        return !existingInstitutes.contains(instituteName) && instituteName.contains(query);
      }
    });
  }

  private Collection<String> getExistingInstituteNames() {
    List<PhysicalResourceGroup> groups = physicalResourceGroupService.findAll();

    return newArrayList(transform(groups, new Function<PhysicalResourceGroup, String>() {
      @Override
      public String apply(PhysicalResourceGroup input) {
        String instituteName = (input == null || (input.getInstitute() == null) ? null : input.getInstitute().getName());

        if (!Strings.isNullOrEmpty(instituteName)) {
          instituteName = instituteName.toLowerCase();
        }

        return instituteName;
      }
    }));
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(final Model uiModel) {
    Collection<Institute> institutes = instituteService.getInstitutes();

    uiModel.addAttribute(MODEL_KEY_LIST, institutes);

    return "institutes/list";
  }

}

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

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static nl.surfnet.bod.web.WebUtils.LIST_POSTFIX;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.web.base.MessageView;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

@RequestMapping(InstituteController.PAGE_URL)
@Controller
public class InstituteController {

  public static final String REFRESH = "/refresh";
  public static final String PAGE_URL = "/institutes";
  public static final String REFRESH_URL = PAGE_URL + REFRESH;

  static final String MODEL_KEY = "institute";
  static final String MODEL_KEY_LIST = MODEL_KEY + LIST_POSTFIX;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource
  private InstituteService instituteService;

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Resource
  private MessageSource messageSource;

  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Collection<Institute> jsonList(@RequestParam(required = false) String q) {
    final Collection<String> existingInstitutes = getExistingInstituteNames();
    final String query = StringUtils.hasText(q) ? q.toLowerCase() : "";

    return filter(instituteService.findAlignedWithIDD(), new Predicate<Institute>() {
      @Override
      public boolean apply(Institute input) {
        String instituteName = nullToEmpty(input.getName()).toLowerCase();

        return !existingInstitutes.contains(instituteName) && !instituteName.isEmpty() && instituteName.contains(query);
      }
    });
  }

  @RequestMapping(value = REFRESH, method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
  public String refreshInstitutes(Model model) {

    MessageView message;

    if (Security.isSelectedNocRole()) {
      logger.info("Manually refreshing institutes...");
      instituteService.refreshInstitutes();

      message = MessageView.createInfoMessage(messageSource,
        "Refreshed institutes", "Refreshed institutes");
    }
    else {
      message = MessageView.createErrorMessage(
          messageSource, "Not a NOC engineer", "Insitutes are not refreshed, you should be a NOC engineer");
    }

    model.addAttribute(MessageView.MODEL_KEY, message);

    return MessageView.PAGE_URL;
  }

  private Collection<String> getExistingInstituteNames() {
    List<PhysicalResourceGroup> groups = physicalResourceGroupService.findAll();

    return newArrayList(transform(groups, new Function<PhysicalResourceGroup, String>() {
      @Override
      public String apply(PhysicalResourceGroup input) {
        String instituteName = input.getInstitute() == null ? "" : nullToEmpty(input.getInstitute().getName());

        return instituteName.toLowerCase();
      }
    }));
  }

}

/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.noc;

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

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
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

  public static final String PAGE_URL = "/noc/institutes";

  static final String MODEL_KEY = "institute";
  static final String MODEL_KEY_LIST = MODEL_KEY + LIST_POSTFIX;

  @Resource
  private InstituteService instituteService;

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

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
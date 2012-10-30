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
package nl.surfnet.bod.web.user;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.util.Orderings;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.VirtualPortJsonView;


@Controller
@RequestMapping("/teams")
public class VirtualResourceGroupController {

  @Resource
  private VirtualResourceGroupService virtualResourceGroupService;

  @RequestMapping(value = "/{id}/ports", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public List<VirtualPortJsonView> listForVirtualResourceGroup(@PathVariable Long id) {
    VirtualResourceGroup group = virtualResourceGroupService.find(id);

    if (group == null || Security.isUserNotMemberOf(group.getSurfconextGroupId())) {
      return Collections.emptyList();
    }

    return Lists.transform(Orderings.vpUserLabelOrdering().sortedCopy(group.getVirtualPorts()),
        new Function<VirtualPort, VirtualPortJsonView>() {
          @Override
          public VirtualPortJsonView apply(VirtualPort port) {
            return new VirtualPortJsonView(port);
          }
        });
  }
}

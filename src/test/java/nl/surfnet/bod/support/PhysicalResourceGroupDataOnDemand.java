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
package nl.surfnet.bod.support;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.InstituteFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PhysicalResourceGroupDataOnDemand {

  private final Random rnd = new SecureRandom();

  private List<PhysicalResourceGroup> data;

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Autowired
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  public PhysicalResourceGroup getNewTransientPhysicalResourceGroup(final int index) {
    PhysicalResourceGroup obj = new PhysicalResourceGroup();
    setInstitutionName(obj, index);
    setName(obj, index);
    return obj;
  }

  public void setInstitutionName(final PhysicalResourceGroup obj, final int index) {
    String institutionName = "institutionName_" + index;
    obj.setInstitute(new InstituteFactory().setId(index).setName(institutionName).create());
  }

  public void setName(final PhysicalResourceGroup obj, final int index) {
    String name = "name_" + index;
    obj.setName(name);
  }

  public PhysicalResourceGroup getSpecificPhysicalResourceGroup(int index) {
    init();
    if (index < 0)
      index = 0;
    if (index > (data.size() - 1))
      index = data.size() - 1;

    PhysicalResourceGroup obj = data.get(index);
    Long id = obj.getId();

    return physicalResourceGroupService.find(id);
  }

  public PhysicalResourceGroup getRandomPhysicalResourceGroup() {
    init();
    PhysicalResourceGroup obj = data.get(rnd.nextInt(data.size()));
    Long id = obj.getId();
    return physicalResourceGroupService.find(id);
  }

  public void init() {
    int from = 0;
    int to = 10;
    data = physicalResourceGroupService.findEntries(from, to);

    if (data == null) {
      throw new IllegalStateException("Find entries implementation for 'PhysicalResourceGroup' illegally returned null");
    }

    if (!data.isEmpty()) {
      return;
    }

    data = new ArrayList<PhysicalResourceGroup>();

    for (int i = 0; i < 10; i++) {
      PhysicalResourceGroup obj = getNewTransientPhysicalResourceGroup(i);
      try {
        physicalResourceGroupService.save(obj);
      }
      catch (ConstraintViolationException e) {
        StringBuilder msg = new StringBuilder();
        for (Iterator<ConstraintViolation<?>> it = e.getConstraintViolations().iterator(); it.hasNext();) {
          ConstraintViolation<?> cv = it.next();
          msg.append("[").append(cv.getConstraintDescriptor()).append(":").append(cv.getMessage()).append("=")
              .append(cv.getInvalidValue()).append("]");
        }
        throw new RuntimeException(msg.toString(), e);
      }

      physicalResourceGroupRepo.flush();
      data.add(obj);
    }
  }
}

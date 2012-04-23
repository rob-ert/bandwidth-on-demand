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
package nl.surfnet.bod.util;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;

import com.google.common.collect.Ordering;

public final class Orderings {

  private static final Ordering<VirtualPort> VP_ORDERING = new Ordering<VirtualPort>() {
    @Override
    public int compare(VirtualPort left, VirtualPort right) {
      return left.getUserLabel().compareTo(right.getUserLabel());
    }
  };

  private static final Ordering<VirtualResourceGroup> VRG_ORDERING = new Ordering<VirtualResourceGroup>() {
    @Override
    public int compare(VirtualResourceGroup left, VirtualResourceGroup right) {
      return left.getName().compareTo(right.getName());
    }
  };

  private static final Ordering<PhysicalResourceGroup> PRG_ORDERING = new Ordering<PhysicalResourceGroup>() {
    @Override
    public int compare(PhysicalResourceGroup left, PhysicalResourceGroup right) {
      return left.getName().compareTo(right.getName());
    }
  };

  private static final Ordering<BodRole> ROLE_ORDERING = new Ordering<BodRole>() {
        @Override
        public int compare(BodRole role1, BodRole role2) {
          return (String.valueOf(role1.getRole().getSortOrder()) + role1.getInstituteName()).compareTo(String
              .valueOf(role2.getRole().getSortOrder()) + role2.getInstituteName());
        }
  };

  private Orderings() {
  }

  public static Ordering<VirtualPort> vpUserLabelOrdering() {
    return VP_ORDERING;
  }

  public static Ordering<VirtualResourceGroup> vrgNameOrdering() {
    return VRG_ORDERING;
  }

  public static Ordering<PhysicalResourceGroup> prgNameOrdering() {
    return PRG_ORDERING;
  }

  public static Ordering<BodRole> bodRoleOrdering() {
    return ROLE_ORDERING;
  }
}

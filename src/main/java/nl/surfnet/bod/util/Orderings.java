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

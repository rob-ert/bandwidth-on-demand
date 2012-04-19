package nl.surfnet.bod.util;

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
}

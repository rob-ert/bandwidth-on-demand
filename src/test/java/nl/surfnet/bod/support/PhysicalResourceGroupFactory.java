package nl.surfnet.bod.support;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

import com.google.common.collect.Lists;

public class PhysicalResourceGroupFactory {

  private static final AtomicInteger count = new AtomicInteger();
  private String name = "First group";
  private String institution = "SURFnet B.V." + count.getAndIncrement();
  private String adminGroup = null;

  private List<PhysicalPort> physicalPorts = Lists.newArrayList();

  public PhysicalResourceGroup create() {
    PhysicalResourceGroup group = new PhysicalResourceGroup();
    group.setName(name);
    group.setInstitutionName(institution);
    group.setAdminGroup(adminGroup);
    group.setPhysicalPorts(physicalPorts);

    return group;
  }

  public PhysicalResourceGroupFactory addPhysicalPort(PhysicalPort... ports) {
    this.physicalPorts.addAll(Arrays.asList(ports));
    return this;
  }

  public PhysicalResourceGroupFactory setName(String name) {
    this.name = name;
    return this;
  }

  public PhysicalResourceGroupFactory setInstitution(String institution) {
    this.institution = institution;
    return this;
  }

  public PhysicalResourceGroupFactory setAdminGroupName(String adminGroup) {
    this.adminGroup = adminGroup;
    return this;
  }

}

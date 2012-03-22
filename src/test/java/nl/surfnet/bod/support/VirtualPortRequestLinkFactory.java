package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.domain.VirtualResourceGroup;

import org.joda.time.LocalDateTime;

public class VirtualPortRequestLinkFactory {

  private VirtualResourceGroup virtualResourceGroup = new VirtualResourceGroupFactory().create();
  private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();
  private LocalDateTime requestDateTime = LocalDateTime.now();
  private String requestor = "urn:truusvisscher";
  private String uuid;
  private String message = "I would like to have a new virtual port to do my work.";
  private Integer minBandwidth = 1000;

  public VirtualPortRequestLink create() {
    VirtualPortRequestLink link = new VirtualPortRequestLink();

    link.setVirtualResourceGroup(virtualResourceGroup);
    link.setPhysicalResourceGroup(physicalResourceGroup);
    link.setRequestDateTime(requestDateTime);
    link.setRequestor(requestor);
    if (uuid != null) {
      link.setUuid(uuid);
    }
    link.setMessage(message);
    link.setMinBandwidth(minBandwidth);

    return link;
  }

  public VirtualPortRequestLinkFactory setMessage(String message) {
    this.message = message;
    return this;
  }

  public VirtualPortRequestLinkFactory setMinBandwidth(Integer minBandwidth) {
    this.minBandwidth = minBandwidth;
    return this;
  }

  public VirtualPortRequestLinkFactory setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
    this.virtualResourceGroup = virtualResourceGroup;
    return this;
  }

  public VirtualPortRequestLinkFactory setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
    return this;
  }
}

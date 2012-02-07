package nl.surfnet.bod.support;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

import org.joda.time.LocalDateTime;

public class ActivationEmailLinkFactory {

  private static final AtomicLong COUNTER = new AtomicLong();

  private Long id = COUNTER.incrementAndGet();
  private Integer version = 0;
  private String uuid = UUID.randomUUID().toString();
  private LocalDateTime creationDateTime = LocalDateTime.now();
  private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();

  public ActivationEmailLink create() {
    ActivationEmailLink link = new ActivationEmailLink();

    link.setId(id);
    link.setVersion(version);
    link.setUuid(uuid);
    link.setCreationDateTime(creationDateTime);
    link.setPhysicalResourceGroup(physicalResourceGroup);

    return link;
  }

  public ActivationEmailLinkFactory setCreationDateTime(LocalDateTime creationDateTime) {
    this.creationDateTime = creationDateTime;
    return this;
  }

  public ActivationEmailLinkFactory setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  public ActivationEmailLinkFactory setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
    return this;
  }

}

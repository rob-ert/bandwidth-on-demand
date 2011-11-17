package org.surfnet.bod.repo;

import org.springframework.roo.addon.layers.repository.jpa.RooRepositoryJpa;
import org.surfnet.bod.domain.PhysicalPort;

@RooRepositoryJpa(domainType = PhysicalPort.class)
public interface PhysicalPortRepository {
}

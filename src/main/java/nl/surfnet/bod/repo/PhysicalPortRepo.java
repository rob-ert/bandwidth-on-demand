package nl.surfnet.bod.repo;

import nl.surfnet.bod.domain.PhysicalPort;

import org.springframework.roo.addon.layers.repository.jpa.RooRepositoryJpa;

@RooRepositoryJpa(domainType = PhysicalPort.class)
public interface PhysicalPortRepo {
}

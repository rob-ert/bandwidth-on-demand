package nl.surfnet.bod.service;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;

public interface NbiPortService {

  List<PhysicalPort> findAll();

  long count();

  PhysicalPort findByName(String name);

}

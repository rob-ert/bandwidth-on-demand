package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;

import org.junit.Test;

public class NbiServiceOfflineTest {

  private NbiServiceOffline subject = new NbiServiceOffline();

  @Test
  public void offlineClientShouldGivePorts() {
    List<PhysicalPort> ports = subject.findAllPhysicalPorts();

    assertThat(ports, hasSize(greaterThan(0)));
  }

  @Test
  public void countShouldMatchNumberOfPorts() {
    List<PhysicalPort> ports = subject.findAllPhysicalPorts();
    long count = subject.getPhysicalPortsCount();

    assertThat(count, is((long) ports.size()));
  }

  @Test
  public void findByNetworkElementId() {
    PhysicalPort port = subject.findAllPhysicalPorts().get(0);

    PhysicalPort foundPort = subject.findPhysicalPortByNetworkElementId(port.getNetworkElementPk());

    assertThat(foundPort.getNetworkElementPk(), is(port.getNetworkElementPk()));
  }

}

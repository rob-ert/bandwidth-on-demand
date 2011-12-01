package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.support.PhysicalPortFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalPortServiceImplTest {

  private PhysicalPortServiceImpl subject = new PhysicalPortServiceImpl();

  @Mock
  private PhysicalPortServiceNbiImpl nbiServiceMock;
  @Mock
  private PhysicalPortServiceRepoImpl repoServiceMock;

  @Before
  public void init() {
    subject.setNbiService(nbiServiceMock);
    subject.setRepoService(repoServiceMock);
  }

  @Test
  public void findAllShouldMergePorts() {
    List<PhysicalPort> nbiPorts = Lists.newArrayList(
        new PhysicalPortFactory().setName("first").create(),
        new PhysicalPortFactory().setName("second").create());
    List<PhysicalPort> repoPorts = Lists.newArrayList(
        new PhysicalPortFactory().setName("first").setId(1L).setVersion(2).create());

    when(nbiServiceMock.findAll()).thenReturn(nbiPorts);
    when(repoServiceMock.findAll()).thenReturn(repoPorts);

    List<PhysicalPort> allPorts = subject.findAll();

    assertThat(allPorts, hasSize(2));
    assertThat(allPorts.get(0).getName(), is("first"));
    assertThat(allPorts.get(0).getId(), is(1L));
    assertThat(allPorts.get(0).getVersion(), is(2));
    assertThat(allPorts.get(1).getName(), is("second"));
  }

  @Test
  public void allUnallocatedPortsShouldNotContainOnesWithId() {
    List<PhysicalPort> nbiPorts = Lists.newArrayList(new PhysicalPortFactory().setName("first").create(),
        new PhysicalPortFactory().setName("second").create());
    List<PhysicalPort> repoPorts = Lists.newArrayList(new PhysicalPortFactory().setName("first").setId(1L).create());

    when(nbiServiceMock.findAll()).thenReturn(nbiPorts);
    when(repoServiceMock.findAll()).thenReturn(repoPorts);

    Collection<PhysicalPort> unallocatedPorts = subject.findUnallocated();

    assertThat(unallocatedPorts, hasSize(1));
    assertThat(unallocatedPorts.iterator().next().getName(), is("second"));
  }

  @Test(expected = IllegalStateException.class)
  public void findAllPortsWithSameNameShouldGiveAnException() {
    List<PhysicalPort> nbiPorts = Lists.newArrayList(new PhysicalPortFactory().setName("first").create());
    List<PhysicalPort> repoPorts = Lists.newArrayList(
        new PhysicalPortFactory().setName("first").create(),
        new PhysicalPortFactory().setName("first").create());

    when(nbiServiceMock.findAll()).thenReturn(nbiPorts);
    when(repoServiceMock.findAll()).thenReturn(repoPorts);

    subject.findAll();
  }

  @Test
  public void findByNameShouldGiveNullIfNotFound() {
    when(nbiServiceMock.findByName("first")).thenReturn(null);
    when(repoServiceMock.findByName("first")).thenReturn(null);

    PhysicalPort port = subject.findByName("first");

    assertThat(port, nullValue());
  }

  @Test
  public void findByNameShouldGiveAMergedPortIfFound() {
    PhysicalPort nbiPort = new PhysicalPortFactory().setName("first").create();
    PhysicalPort repoPort = new PhysicalPortFactory().setId(1L).setName("first").create();

    when(nbiServiceMock.findByName("first")).thenReturn(nbiPort);
    when(repoServiceMock.findByName("first")).thenReturn(repoPort);

    PhysicalPort port = subject.findByName("first");

    assertThat(port.getName(), is("first"));
    assertThat(port.getId(), is(1L));
  }

  @Test
  public void updateShouldCallUpdateOnRepo() {
    PhysicalPort port = new PhysicalPortFactory().create();

    subject.update(port);

    verify(repoServiceMock, only()).update(port);
  }

  @Test
  public void deleteShouldCallDeleteOnRepo() {
    PhysicalPort port = new PhysicalPortFactory().create();

    subject.delete(port);

    verify(repoServiceMock, only()).delete(port);
  }

  @Test
  public void saveShouldCallSaveOnRepo() {
    PhysicalPort port = new PhysicalPortFactory().create();

    subject.save(port);

    verify(repoServiceMock, only()).save(port);
  }

  @Test
  public void findShouldCallFindOnRepo() {
    subject.find(1L);

    verify(repoServiceMock, only()).find(1L);
  }

  @Test
  public void countShouldCallCountOnNbi() {
    subject.count();

    verify(nbiServiceMock, only()).count();
  }

}

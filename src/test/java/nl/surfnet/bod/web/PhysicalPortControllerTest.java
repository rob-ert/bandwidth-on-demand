package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static nl.surfnet.bod.web.PhysicalPortController.MODEL_KEY;
import static nl.surfnet.bod.web.PhysicalPortController.MODEL_KEY_LIST;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalPortFactory;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.Model;

import com.google.common.collect.Lists;

public class PhysicalPortControllerTest {

  private PhysicalPortController subject;

  private PhysicalPortService physicalPortServiceMock;

  @Before
  public void initController() {
    subject = new PhysicalPortController();
    physicalPortServiceMock = mock(PhysicalPortService.class);
    subject.setPhysicalPortService(physicalPortServiceMock);
  }

  @Test
  public void listAllPortsShouldSetPortsAndMaxPages() {
    Model model = new ModelStub();
    List<PhysicalPort> ports = Lists.newArrayList(new PhysicalPortFactory().create());
    when(physicalPortServiceMock.findEntries(eq(0), anyInt())).thenReturn(ports);

    subject.list(1, model);

    assertThat(model.asMap(), hasEntry(MODEL_KEY_LIST, Object.class.cast(ports)));
    assertThat(model.asMap(), hasEntry("maxPages", Object.class.cast(1)));
  }

  @Test
  public void listAllPortsWithoutAPageParam() {
    Model model = new ModelStub();
    List<PhysicalPort> ports = Lists.newArrayList(new PhysicalPortFactory().create());
    when(physicalPortServiceMock.findEntries(eq(0), anyInt())).thenReturn(ports);

    subject.list(null, model);

    assertThat(model.asMap(), hasEntry(MODEL_KEY_LIST, Object.class.cast(ports)));
    assertThat(model.asMap(), hasEntry("maxPages", Object.class.cast(1)));
  }

  @Test
  public void listAllUnallocatedPortsShouldSetPortsAndMaxPages() {
    Model model = new ModelStub();
    List<PhysicalPort> ports = Lists.newArrayList(new PhysicalPortFactory().create());
    when(physicalPortServiceMock.findUnallocatedEntries(eq(0), anyInt())).thenReturn(ports);

    subject.listUnallocated(1, model);

    assertThat(model.asMap(), hasEntry(MODEL_KEY_LIST, Object.class.cast(ports)));
    assertThat(model.asMap(), hasEntry("maxPages", Object.class.cast(1)));
  }

  @Test
  public void showNonExistingPort() {
    Model model = new ModelStub();
    when(physicalPortServiceMock.findByName("12:00/port1")).thenReturn(null);

    subject.show("12:00/port1", model);

    assertThat(model.asMap(), hasEntry(is(MODEL_KEY), nullValue()));
  }

  @Test
  public void showExistingPort() {
    Model model = new ModelStub();
    PhysicalPort port = new PhysicalPortFactory().create();
    when(physicalPortServiceMock.findByName("12:00/port1")).thenReturn(port);

    subject.show("12:00/port1", model);

    assertThat(model.asMap(), hasEntry(MODEL_KEY, Object.class.cast(port)));
  }

  @Test
  public void updateForm() {
    Model model = new ModelStub();
    PhysicalPort port = new PhysicalPortFactory().create();
    when(physicalPortServiceMock.findByName("00:00/port2")).thenReturn(port);

    subject.updateForm("00:00/port2", model);

    assertThat(model.asMap(), hasEntry(MODEL_KEY, Object.class.cast(port)));
  }

  @Test
  public void deleteShouldStayOnSamePage() {
    Model model = new ModelStub();
    PhysicalPort port = new PhysicalPortFactory().create();
    when(physicalPortServiceMock.findByName("port_name")).thenReturn(port);

    subject.delete("port_name", 3, model);

    assertThat(model.asMap(), hasEntry("page", Object.class.cast("3")));

    verify(physicalPortServiceMock, times(1)).delete(port);
  }

}

/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.web.noc;

import static nl.surfnet.bod.web.WebUtils.MAX_PAGES_KEY;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.web.noc.PhysicalPortController.CreatePhysicalPortCommand;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalPortControllerTest {

  @InjectMocks
  private PhysicalPortController subject;

  @Mock
  private PhysicalPortService physicalPortServiceMock;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  @Mock
  private MessageSource messageSource;

  @Test
  public void listAllPortsShouldSetPortsAndMaxPages() {
    Model model = new ModelStub();
    List<PhysicalPort> ports = Lists.newArrayList(new PhysicalPortFactory().create());
    when(physicalPortServiceMock.findAllocatedEntries(eq(0), anyInt(), org.mockito.Matchers.any(Sort.class))).thenReturn(ports);

    subject.list(1, null, null, model);

    assertThat(model.asMap(), hasEntry("list", Object.class.cast(ports)));
    assertThat(model.asMap(), hasEntry(MAX_PAGES_KEY, Object.class.cast(1)));
  }

  @Test
  public void listAllPortsWithoutAPageParam() {
    Model model = new ModelStub();
    List<PhysicalPort> ports = Lists.newArrayList(new PhysicalPortFactory().create());
    when(physicalPortServiceMock.findAllocatedEntries(eq(0), anyInt(), org.mockito.Matchers.any(Sort.class))).thenReturn(ports);

    subject.list(null, null, null, model);

    assertThat(model.asMap(), hasEntry("list", Object.class.cast(ports)));
    assertThat(model.asMap(), hasEntry(MAX_PAGES_KEY, Object.class.cast(1)));
  }

  @Test
  public void listAllUnallocatedPortsShouldSetPortsAndMaxPages() {
    Model model = new ModelStub();
    List<PhysicalPort> ports = Lists.newArrayList(new PhysicalPortFactory().create());
    when(physicalPortServiceMock.findUnallocatedEntries(eq(0), anyInt())).thenReturn(ports);

    subject.listUnallocated(1, model);

    assertThat(model.asMap(), hasEntry("list", Object.class.cast(ports)));
    assertThat(model.asMap(), hasEntry(MAX_PAGES_KEY, Object.class.cast(1)));
  }

  @Test
  public void updateForm() {
    Model model = new ModelStub();
    PhysicalPort port = new PhysicalPortFactory().setNetworkElementPk("00:00/port2").create();

    when(physicalPortServiceMock.findByNetworkElementPk("00:00/port2")).thenReturn(port);

    subject.updateForm("00:00/port2", model);

    assertThat(model.asMap(), hasKey("createPhysicalPortCommand"));
    assertThat(((CreatePhysicalPortCommand) model.asMap().get("createPhysicalPortCommand")).getNetworkElementPk(),
        is("00:00/port2"));
  }

  @Test
  public void updateShouldGoToFreePortsAndShowMessage() {
    RedirectAttributes model = new ModelStub();
    PhysicalPort port = new PhysicalPortFactory().create();
    BindingResult result = new BeanPropertyBindingResult(port, "physicalPort");

    when(messageSource.getMessage(anyString(), isNull(Object[].class), org.mockito.Matchers.any(Locale.class)))
        .thenReturn("{} {}");
    when(physicalPortServiceMock.findByNetworkElementPk(port.getNetworkElementPk())).thenReturn(port);

    String page = subject.update(new CreatePhysicalPortCommand(port), result, model, model);

    assertThat(page, is("redirect:physicalports"));
    assertThat(model.getFlashAttributes(), hasKey("infoMessages"));

    @SuppressWarnings("unchecked")
    String flashMessage = ((List<String>) model.getFlashAttributes().get("infoMessages")).get(0);
    assertThat(flashMessage, containsString(port.getNocLabel()));
    assertThat(flashMessage, containsString(port.getPhysicalResourceGroup().getInstitute().getName()));

    verify(physicalPortServiceMock).save(port);
  }

  @Test
  public void deleteShouldStayOnSamePage() {
    Model model = new ModelStub();
    PhysicalPort port = new PhysicalPortFactory().create();
    when(physicalPortServiceMock.findByNetworkElementPk("port_name")).thenReturn(port);

    subject.delete("port_name", 3, model);

    assertThat(model.asMap(), hasEntry(PAGE_KEY, Object.class.cast("3")));

    verify(physicalPortServiceMock, times(1)).delete(port);
  }

}

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
package nl.surfnet.bod.web.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.ActivationEmailLinkFactory;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.web.WebUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RunWith(MockitoJUnitRunner.class)
public class ActivationEmailControllerTest {

  @InjectMocks
  private ActivationEmailController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Mock
  private ActivationEmailLink<PhysicalResourceGroup> linkMock;

  @SuppressWarnings("unused")
  @Mock
  private InstituteService instituteService;

  private ActivationEmailLink<PhysicalResourceGroup> link = new ActivationEmailLinkFactory<PhysicalResourceGroup>()
      .create();

  private ModelStub model = new ModelStub();

  @Test
  public void physicalResourceGroupShouldBeActivated() {

    when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(link);

    String page = subject.activateEmail("1234567890", model);

    assertThat(page, is("manager/emailConfirmed"));
    assertThat(model.asMap(), hasEntry("physicalResourceGroup", Object.class.cast(link.getSourceObject())));

    verify(physicalResourceGroupServiceMock, times(1)).activate(any((ActivationEmailLink.class)));
  }

  @Test
  public void activationLinkIsNotValidAnymore() {
    when(linkMock.isValid()).thenReturn(false);
    when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(linkMock);

    String page = subject.activateEmail("1234567890", new ModelStub());

    verify(physicalResourceGroupServiceMock, times(0)).activate(any((ActivationEmailLink.class)));
    assertThat(page, is("manager/linkNotValid"));
  }

  @Test
  public void activationLinkIsNotValid() {
    when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(null);

    String page = subject.activateEmail("1234567890", new ModelStub());

    verify(physicalResourceGroupServiceMock, times(0)).activate(any((ActivationEmailLink.class)));
    assertThat(page, is("index"));
  }

  @Test
  public void activationLinkIsAlreadyActivated() {
    when(linkMock.isActivated()).thenReturn(true);
    when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(linkMock);

    String page = subject.activateEmail("1234567890", new ModelStub());
    verify(physicalResourceGroupServiceMock, times(0)).activate(any((ActivationEmailLink.class)));
    assertThat(page, is("manager/linkActive"));
  }

  @Test
  public void shouldRequestNewLink() {
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();
    BindingResult bindingResultMock = mock(BindingResult.class);
    Model modelMock = new ModelStub();
    RedirectAttributes redirectAttributesMock = new ModelStub();

    when(physicalResourceGroupServiceMock.find(anyLong())).thenReturn(physicalResourceGroup);
    when(linkMock.getToEmail()).thenReturn(physicalResourceGroup.getManagerEmail());
    when(linkMock.getSourceObject()).thenReturn(physicalResourceGroup);
    when(physicalResourceGroupServiceMock.sendAndPersistActivationRequest(physicalResourceGroup)).thenReturn(linkMock);
    subject.create(physicalResourceGroup, bindingResultMock, modelMock, redirectAttributesMock);

    verify(physicalResourceGroupServiceMock).sendAndPersistActivationRequest(physicalResourceGroup);

    assertThat(redirectAttributesMock.getFlashAttributes().keySet(), contains(WebUtils.INFO_MESSAGES_KEY));
    assertThat((String) redirectAttributesMock.getFlashAttributes().get(WebUtils.INFO_MESSAGES_KEY),
        containsString(physicalResourceGroup.getName()));
    assertThat((String) redirectAttributesMock.getFlashAttributes().get(WebUtils.INFO_MESSAGES_KEY),
        containsString(physicalResourceGroup.getManagerEmail()));
  }

}

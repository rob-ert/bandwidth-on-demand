package nl.surfnet.bod.web.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.ActivationEmailLinkFactory;
import nl.surfnet.bod.support.ModelStub;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
}

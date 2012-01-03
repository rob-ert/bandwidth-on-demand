package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.support.PhysicalPortFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationConversionServiceFactoryBeanTest {

  @InjectMocks
  private ApplicationConversionServiceFactoryBean subject;

  @Mock
  private PhysicalPortService physicalPortServiceMock;

  @Test
  public void convetIdToPhysicalPort() {
    PhysicalPort port = new PhysicalPortFactory().create();

    when(physicalPortServiceMock.find(1L)).thenReturn(port);

    PhysicalPort convertedPort = subject.getIdToPhysicalPortConverter().convert(1L);

    assertThat(convertedPort, is(port));
  }

}

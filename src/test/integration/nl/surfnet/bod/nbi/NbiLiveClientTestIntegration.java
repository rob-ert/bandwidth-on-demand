package nl.surfnet.bod.nbi;

import static org.junit.Assert.assertEquals;

import java.util.List;

import nl.surfnet.bod.nbi.generated.TerminationPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
public class NbiLiveClientTestIntegration {

  @SuppressWarnings("unused")
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private NbiClient nbiClient;

  @Test
  public void testFindAllPortsWithDetails() {
    final List<TerminationPoint> allTerminationPoints = nbiClient.findAllPorts();
    assertEquals(260, allTerminationPoints.size());
  }

  @Test
  public void testFindPortByNameWithDetails() {
    final String portName = "00:03:18:bb:5a:00_Port1/30";
    final TerminationPoint terminationPoints = nbiClient.findPortsByName(portName);
    assertEquals(portName, terminationPoints.getPortDetail().getName());
  }

}

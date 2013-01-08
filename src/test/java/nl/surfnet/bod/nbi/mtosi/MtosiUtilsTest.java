package nl.surfnet.bod.nbi.mtosi;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MtosiUtilsTest {

  @Test
  public void convertPtp() {
    assertThat(
        MtosiUtils.physicalTerminationPointToNmsPortId("/rack=1/shelf=1/slot=1/port=48"),
        is("1-1-1-48"));
  }

  @Test
  public void convertPtpWithSubSlot() {
    assertThat(
        MtosiUtils.physicalTerminationPointToNmsPortId("/rack=1/shelf=1/slot=3/sub_slot=1/port=5"),
        is("1-1-3-1-5"));
  }

  @Test
  public void convertNmsPortId() {
    assertThat(
      MtosiUtils.nmsPortIdToPhysicalTerminationPoint("1-2-3-4"),
      is("/rack=1/shelf=2/slot=3/port=4"));
  }

  @Test
  public void convertNmsPortIdWithSubSlot() {
    assertThat(
      MtosiUtils.nmsPortIdToPhysicalTerminationPoint("2-3-4-5-10"),
      is("/rack=2/shelf=3/slot=4/sub_slot=5/port=10"));
  }
}

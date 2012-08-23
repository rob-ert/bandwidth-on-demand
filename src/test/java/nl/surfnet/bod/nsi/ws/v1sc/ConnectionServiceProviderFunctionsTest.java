package nl.surfnet.bod.nsi.ws.v1sc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.joda.time.DateTime;
import org.junit.Test;

public class ConnectionServiceProviderFunctionsTest {

  @Test
  public void calculateEndTimeShouldNotChangeStartTime() throws DatatypeConfigurationException {
    Duration duration = DatatypeFactory.newInstance().newDuration(true, 0, 0, 0, 2, 10, 0); // 2 hours, 10 minutes
    Date startDate = new DateTime(2012, 1, 1, 13, 0, 0).toDate();

    Date endTime = ConnectionServiceProviderFunctions.calculateEndTime(startDate, duration);

    DateTime startDateJoda = new DateTime(startDate);
    assertThat(startDateJoda.getHourOfDay(), is(13));
    assertThat(startDateJoda.getMinuteOfHour(), is(0));

    DateTime endDateJoda = new DateTime(endTime);
    assertThat(endDateJoda.getHourOfDay(), is(15));
    assertThat(endDateJoda.getMinuteOfHour(), is(10));
  }

}

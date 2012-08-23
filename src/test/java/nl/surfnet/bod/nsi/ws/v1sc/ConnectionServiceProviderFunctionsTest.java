package nl.surfnet.bod.nsi.ws.v1sc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.support.ReserveRequestTypeFactory;

import org.joda.time.DateTime;
import org.junit.Test;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;

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

  @Test
  public void reserveToConnectionWithADuration() throws DatatypeConfigurationException {
    ReserveRequestType reserveRequest = new ReserveRequestTypeFactory()
      .setScheduleStartTime(
          DatatypeFactory.newInstance().newXMLGregorianCalendar(2012, 5, 18, 14, 0, 0, 0, DatatypeConstants.FIELD_UNDEFINED))
      .setScheduleEndTime(null)
      .setDuration(DatatypeFactory.newInstance().newDuration(true, 0, 0, 2, 5, 10, 0))
      .setConnectionId("connectionId1").create();

    Connection connection = ConnectionServiceProviderFunctions.RESERVE_REQUEST_TO_CONNECTION.apply(reserveRequest);

    assertThat(connection.getConnectionId(), is("connectionId1"));
    DateTime startTime = new DateTime(connection.getStartTime());
    assertThat(startTime, is(new DateTime(2012, 5, 18, 14, 0)));

    DateTime endTime = new DateTime(connection.getEndTime());
    assertThat(endTime, is(new DateTime(2012, 5, 20, 19, 10)));
  }

}

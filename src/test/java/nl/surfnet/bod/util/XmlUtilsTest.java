package nl.surfnet.bod.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;


public class XmlUtilsTest {

  @Test
  public void shouldPreserveTimeZoneWithPositieveOffsetFromXml() throws DatatypeConfigurationException {
    int offSetInHours = 2;
    String xmlDate = String.format("2012-09-26T11:05:10%0+3d:00", offSetInHours);
    XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlDate);

    DateTime timeStamp = XmlUtils.getDateFrom(calendar).get();
    assertThat(timeStamp.getZone().getOffset(timeStamp.getMillis()), is(offSetInHours * 60 * 60 * 1000));
    assertThat(timeStamp.getMillis(), is(calendar.toGregorianCalendar().getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneWithNegativeOffsetFromXml() throws DatatypeConfigurationException {
    int offSetInHours = -4;

    String xmlDate = String.format("2012-09-26T11:05:10%0+3d:00", offSetInHours);
    XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlDate);

    DateTime timeStamp = XmlUtils.getDateFrom(calendar).get();
    assertThat(timeStamp.getZone().getOffset(timeStamp.getMillis()), is(offSetInHours * 60 * 60 * 1000));
    assertThat(timeStamp.getMillis(), is(calendar.toGregorianCalendar().getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneZeroOffsetFromXml() throws DatatypeConfigurationException {
    int offSetInHours = 0;

    String xmlDate = String.format("2012-09-26T11:05:10%0+3d:00", offSetInHours);
    XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlDate);

    DateTime timeStamp = XmlUtils.getDateFrom(calendar).get();
    assertThat(timeStamp.getZone().getOffset(timeStamp.getMillis()), is(offSetInHours * 60 * 60 * 1000));
    assertThat(timeStamp.getMillis(), is(calendar.toGregorianCalendar().getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneUTCFromXml() throws DatatypeConfigurationException {
    String xmlDate = "2012-09-26T11:05:10Z";
    XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlDate);

    DateTime timeStamp = XmlUtils.getDateFrom(calendar).get();
    assertThat(timeStamp.getZone().getOffset(timeStamp.getMillis()), is(0));
    assertThat(timeStamp.getMillis(), is(calendar.toGregorianCalendar().getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneWithPositieveOffsetToXml() throws DatatypeConfigurationException {
    int offSetInHours = 4;
    DateTime timeStamp = new DateTime(2012, 9, 26, 14, 40, 0, 0, DateTimeZone.forOffsetHours(offSetInHours));
    XMLGregorianCalendar xmlDateTime = XmlUtils.getXmlTimeStampFromDateTime(timeStamp).get();

    assertThat(xmlDateTime.getTimezone(), is(offSetInHours * 60));
    assertThat(xmlDateTime.toGregorianCalendar().getTimeInMillis(), is(xmlDateTime.toGregorianCalendar()
        .getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneWithNegativeOffsetToXml() throws DatatypeConfigurationException {
    int offSetInHours = -12;
    DateTime timeStamp = new DateTime(2012, 9, 26, 14, 40, 0, 0, DateTimeZone.forOffsetHours(offSetInHours));
    XMLGregorianCalendar xmlDateTime = XmlUtils.getXmlTimeStampFromDateTime(timeStamp).get();

    assertThat(xmlDateTime.getTimezone(), is(offSetInHours * 60));
    assertThat(xmlDateTime.toGregorianCalendar().getTimeInMillis(), is(xmlDateTime.toGregorianCalendar()
        .getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneWithZeroOffsetToXml() throws DatatypeConfigurationException {
    int offSetInHours = 0;
    DateTime timeStamp = new DateTime(2012, 9, 26, 14, 40, 0, 0, DateTimeZone.forOffsetHours(offSetInHours));
    XMLGregorianCalendar xmlDateTime = XmlUtils.getXmlTimeStampFromDateTime(timeStamp).get();

    assertThat(xmlDateTime.getTimezone(), is(offSetInHours * 60));
    assertThat(xmlDateTime.toGregorianCalendar().getTimeInMillis(), is(xmlDateTime.toGregorianCalendar()
        .getTimeInMillis()));
  }

  @Test
  public void shouldPreserveTimeZoneWithUTCToXml() throws DatatypeConfigurationException {
    DateTime timeStamp = new DateTime(2012, 9, 26, 14, 40, 0, 0, DateTimeZone.UTC);
    XMLGregorianCalendar xmlDateTime = XmlUtils.getXmlTimeStampFromDateTime(timeStamp).get();

    assertThat(xmlDateTime.getTimezone(), is(0));
    assertThat(xmlDateTime.toGregorianCalendar().getTimeInMillis(), is(xmlDateTime.toGregorianCalendar()
        .getTimeInMillis()));
  }

}

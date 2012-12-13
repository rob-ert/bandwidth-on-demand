package nl.surfnet.bod.nbi.mtosi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.xml.ws.Holder;

import org.joda.time.DateTime;
import org.junit.Test;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;


public class HeaderBuilderTest {

  @Test
  public void reserveHeaderShouldFileHeaders() {
    final String endPoint = "http://nonexisting.example.com/wsendpoint";

    Holder<Header> holder = HeaderBuilder.buildReserveHeader(endPoint);

    Header header = holder.value;

    assertThat(header.getDestinationURI(), is(endPoint));
    assertThat(header.getActivityName(), is("reserve"));
    assertThat(header.getTimestamp().getYear(), is(DateTime.now().getYear()));
  }

  @Test
  public void inventoryHeaderShouldFileHeaders() {
    final String endPoint = "http://nonexisting.example.com/wsendpoint";

    Holder<Header> holder = HeaderBuilder.buildInventoryHeader(endPoint);

    Header header = holder.value;

    assertThat(header.getDestinationURI(), is(endPoint));
    assertThat(header.getActivityName(), is("getServiceInventory"));
    assertThat(header.getTimestamp().getYear(), is(DateTime.now().getYear()));
  }
}

package nl.surfnet.bod.nbi.mtosi;

import javax.xml.ws.Holder;

import nl.surfnet.bod.util.XmlUtils;

import org.joda.time.DateTime;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationPatternType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationStyleType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.hdr.v1.MessageTypeType;

public class HeaderBuilder {

  private HeaderBuilder() {
  }

  private static Holder<Header> buildHeader(String endPoint, String activityName, String msgName) {
    final Header header = new Header();
    header.setDestinationURI(endPoint);
    header.setCommunicationStyle(CommunicationStyleType.RPC);
    header.setCommunicationPattern(CommunicationPatternType.SIMPLE_RESPONSE);
    header.setTimestamp(XmlUtils.getXmlTimeStampFromDateTime(DateTime.now()).get());
    header.setActivityName(activityName);
    header.setMsgName(msgName);
    // TODO should change sender URI?
    header.setSenderURI("http://localhost:9009");
    header.setMsgType(MessageTypeType.REQUEST);

    return new Holder<Header>(header);
  }

  public static Holder<Header> buildReserveHeader(String endPoint) {
    return buildHeader(endPoint, "reserve", "reserveRequest");
  }

  public static Holder<Header> buildInventoryHeader(String endPoint) {
    return buildHeader(endPoint, "getServiceInventory", "getServiceInventoryRequest");
  }

}

package nl.surfnet.bod.domain;

import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify;

public class MtosiNotificationHolder {

  private final Header header;
  private final Notify body;

  public MtosiNotificationHolder(Header header, Notify body) {
    super();
    this.header = header;
    this.body = body;
  }

  public final Header getHeader() {
    return header;
  }

  public final Notify getBody() {
    return body;
  }

}

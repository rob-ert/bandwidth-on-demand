package nl.surfnet.bod.nsi.v2;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ogf.schemas.nsi._2013._12.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._12.framework.headers.SessionSecurityAttrType;


public class UpdateNsiHeadersForAckHandlerTest {

  @Test
  public void should_remove_reply_to_from_ack_headers() {
    CommonHeaderType headers = new CommonHeaderType().withReplyTo("http://example.com/reply");

    UpdateNsiHeadersForAckHandler.updateAcknowledgmentHeaders(headers);

    assertThat(headers.getReplyTo(), is(nullValue()));
  }

  @Test
  public void should_remove_session_security_attrs_from_ack_headers() {
    CommonHeaderType headers = new CommonHeaderType().withSessionSecurityAttr(new SessionSecurityAttrType(), new SessionSecurityAttrType());

    UpdateNsiHeadersForAckHandler.updateAcknowledgmentHeaders(headers);

    assertThat(headers.getSessionSecurityAttr(), is(empty()));
  }
}

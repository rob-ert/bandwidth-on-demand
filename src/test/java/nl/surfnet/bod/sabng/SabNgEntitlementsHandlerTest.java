package nl.surfnet.bod.sabng;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class SabNgEntitlementsHandlerTest {

  private static final String NAME_ID = "urn:test:user";
  private static final String ISSUER = "dev";
  private static final String RESPONSE_LOCATION = "/xmlsabng/response-entitlement.xml";

  private final SabNgEntitlementsHandler subject = new SabNgEntitlementsHandler();

  @Test
  public void shouldCreateRequestWithParameters() {
    String request = subject.createRequest(ISSUER, NAME_ID);

    assertThat(request, containsString(NAME_ID + "</saml:NameID>"));
    assertThat(request, containsString(ISSUER + "</saml:Issuer>"));
  }

  @Test
  public void shouldRetrieveEntitlementsFromResponse() throws IOException {
    List<String> entitlements = subject.retrieveEntitlements(this.getClass().getResourceAsStream(RESPONSE_LOCATION));

  }
}

package nl.surfnet.bod.sabng;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.surfnet.bod.util.Environment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SabNgEntitlementsHandlerTest {

  private static final String NAME_ID = "urn:test:user";
  private static final String ISSUER = "dev";
  private static final String RESPONSE_LOCATION = "/xmlsabng/response-entitlement.xml";

  @InjectMocks
  private SabNgEntitlementsHandler subject;

  @Mock
  private Environment bodEnvironment;

  @Test
  public void shouldCreateRequestWithParameters() {
    String request = subject.createRequest(ISSUER, NAME_ID);

    assertThat(request, containsString(NAME_ID + "</saml:NameID>"));
    assertThat(request, containsString(ISSUER + "</saml:Issuer>"));
  }

  @Test
  public void shouldMatchEntitlement() throws IOException {
    when(bodEnvironment.getBodAdminEntitlement()).thenReturn("Instellingsbevoegde");
    assertTrue(subject.retrieveEntitlements(this.getClass().getResourceAsStream(RESPONSE_LOCATION)));
  }

  @Test
  public void shouldNotMatchEntitlement() throws IOException {
    when(bodEnvironment.getBodAdminEntitlement()).thenReturn("no-match");
    assertFalse(subject.retrieveEntitlements(this.getClass().getResourceAsStream(RESPONSE_LOCATION)));
  }
}

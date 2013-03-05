/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.sabng;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.xpath.XPathExpressionException;

import nl.surfnet.bod.util.Environment;

import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SabNgEntitlementsHandlerTest {

  private static final String REQ_ID = "d6873bc2-809d-482c-a24c-ccc51d829cf8";
  private static final String NAME_ID = "urn:test:user";
  private static final String ISSUER = "dev";
  private InputStream responseStream;

  @InjectMocks
  private SabNgEntitlementsHandler subject;

  @Mock
  private Environment bodEnvironment;

  @Mock
  private HttpClient httpClient;

  @Before
  public void setUp() {
    responseStream = SabNgEntitlementsHandlerTest.class.getResourceAsStream("/xmlsabng/response-entitlement.xml");
    subject.setSabEndPoint("http://localhost:7000/sap");
    subject.setSabPassword("secret");
    subject.setSabUser("user");
  }

  @Test
  public void whenSabDisabledShouldNotCallService() {
    when(bodEnvironment.isSabEnabled()).thenReturn(false);

    subject.checkInstitutes("urn:henk");

    verifyZeroInteractions(httpClient);
  }

  @Test
  public void shouldCreateRequestWithParameters() {
    String request = subject.createRequest(REQ_ID, ISSUER, NAME_ID);

    assertThat(request, containsString("ID=\"" + REQ_ID));
    assertThat(request, containsString(NAME_ID + "</saml:NameID>"));
    assertThat(request, containsString(ISSUER + "</saml:Issuer>"));
  }

  @Test
  public void shouldMatchEntitlement() throws IOException, XPathExpressionException {
    subject.setSabRole("Instellingsbevoegde");

    assertThat(subject.getInstitutesWhichHaveBoDAdminEntitlement(REQ_ID, responseStream), contains("SURFNET",
        "WESAIDSO"));
  }

  @Test
  public void shouldMatchOnInfra() throws XPathExpressionException {
    subject.setSabRole("Infraverantwoordelijke");

    assertThat(subject.getInstitutesWhichHaveBoDAdminEntitlement(REQ_ID, responseStream), contains("SURFNET"));
  }

  @Test
  public void shouldMatchOnBeveiliging() throws XPathExpressionException {
    subject.setSabRole("Beveiligingsverantwoordelijke");

    assertThat(subject.getInstitutesWhichHaveBoDAdminEntitlement(REQ_ID, responseStream), contains("WESAIDSO"));
  }

  @Test
  public void shouldNotMatchEntitlement() throws IOException, XPathExpressionException {
    subject.setSabRole("no-match");

    assertThat(subject.getInstitutesWhichHaveBoDAdminEntitlement(REQ_ID, responseStream), hasSize(0));
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowParseException() throws XPathExpressionException, IOException {
    StringEntity stringEntity = new StringEntity(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \\" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"> \\" +
            "<SOAP-ENV:Body>Garbage</SOAP-ENV:Body></SOAP-ENV:Envelope>");

    subject.getInstitutesWhichHaveBoDAdminEntitlement("id", stringEntity.getContent());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotMatchInResponseToId() throws XPathExpressionException {
    subject.getInstitutesWhichHaveBoDAdminEntitlement("no-match", responseStream);
  }

  @Test
  public void shouldHaveValidIssueInstant() {
    DateTime now = DateTime.now();

    assertTrue(subject.validateIssueInstant(now, now.minusHours(1), now.plusHours(1)));
  }

  public void shouldHaveEqualIssueInstant() {
    DateTime now = DateTime.now();

    assertTrue(subject.validateIssueInstant(now, now, now));
  }

  @Test
  public void shouldFailSinceIssueInstantIsToEarly() {
    DateTime now = DateTime.now();

    assertFalse(subject.validateIssueInstant(now, now.plusHours(1), now));
  }

  @Test
  public void shouldFailsSinceIssueInstantIsToLate() {
    DateTime now = DateTime.now();

    assertFalse(subject.validateIssueInstant(now, now, now.minusHours(1)));
  }

}
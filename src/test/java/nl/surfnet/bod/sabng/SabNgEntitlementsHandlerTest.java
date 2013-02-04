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

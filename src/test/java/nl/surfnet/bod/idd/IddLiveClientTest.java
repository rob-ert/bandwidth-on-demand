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
package nl.surfnet.bod.idd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collection;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.support.MockHttpServer;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;

public class IddLiveClientTest {

  private static MockHttpServer server;

  private IddLiveClient subject;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @BeforeClass
  public static void initAndStartServer() throws Exception {
    server = new MockHttpServer(8088);
    server.withBasicAuthentication("Donald", "secret");
    server.startServer();
  }

  @AfterClass
  public static void stopServer() throws Exception {
    server.stopServer();
  }

  @Before
  public void setUp() {
    subject = new IddLiveClient("Donald", "secret", "http://localhost:8088/getKlant.php");

    server.addResponse("/getKlant.php", new ClassPathResource("idd_response_with_5_klanten.xml"));
  }

  @Test
  public void shouldGet5Klanten() {
    Collection<Institute> result = subject.getInstitutes();

    assertThat(result, hasSize(5));
  }

  @Test
  public void wrongPasswordShouldGiveException() {
    subject = new IddLiveClient("Wrong", "secret", "http://localhost:8088/getKlant.php");
    thrown.expect(RuntimeException.class);
    thrown.expectMessage(containsString("401"));

    subject.getInstitutes();
  }

}

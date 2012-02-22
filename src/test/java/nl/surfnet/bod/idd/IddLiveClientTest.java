/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.idd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collection;

import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.support.MockHttpServer;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;

public class IddLiveClientTest {

  private static MockHttpServer server;

  private IddLiveClient subject = new IddLiveClient();

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
    subject.setEndPoint("http://localhost:8088/getKlant.php");
    subject.setUsername("Donald");
    subject.setPassword("secret");

    server.addResponse("/getKlant.php", new ClassPathResource("idd_response_with_5_klanten.xml"));
  }

  @Test
  public void shouldGet5Klanten() {
    Collection<Klanten> result = subject.getKlanten();

    assertThat(result, hasSize(5));
  }

  @Test
  public void getKlantByExistingId() {
    Klanten result = subject.getKlantById(564L);

    assertThat(result.getKlant_id(), is(564));
  }

  @Test
  public void getKlantByNonExistingId() {
    Klanten result = subject.getKlantById(-9999L);

    assertThat(result, nullValue());
  }

  @Test
  public void getKlantByIdShouldBeCached() {
    subject.refreshCache();
    server.removeResponse("/getKlant.php");

    Klanten result = subject.getKlantById(564L);

    assertThat(result.getKlant_id(), is(564));
  }

  @Test
  public void wrongPasswordShouldGiveException() {
    subject.setUsername("wrong");
    thrown.expect(RuntimeException.class);
    thrown.expectMessage(containsString("401"));

    subject.getKlanten();
  }

}

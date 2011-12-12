package nl.surfnet.bod.idd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collection;

import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.support.MockHttpServer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
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

  @Test
  public void shouldGet5Klanten() {
    subject.setEndPoint("http://localhost:8088/getKlant.php");
    subject.setUsername("Donald");
    subject.setPassword("secret");

    server.addResponse("/getKlant.php", new ClassPathResource("idd_response_with_5_klanten.xml"));

    Collection<Klanten> result = subject.getKlanten();

    assertThat(result, hasSize(5));
  }

  @Test
  public void wrongPasswordShouldGiveException() {
    subject.setEndPoint("http://localhost:8088/getKlant.php");
    subject.setUsername("Donald");
    subject.setPassword("wrong");

    thrown.expect(RuntimeException.class);
    thrown.expectMessage(containsString("401"));

    subject.getKlanten();
  }

}

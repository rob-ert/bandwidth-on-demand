package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Collection;

import nl.surfnet.bod.support.MockHttpServer;
import nl.surfnet.bod.util.Environment;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensocial.models.Group;
import org.springframework.core.io.ByteArrayResource;

public class GroupOpenSocialServiceTest {

  private static MockHttpServer mockServer = new MockHttpServer(8088);

  private GroupOpenSocialService subject = new GroupOpenSocialService();

  @BeforeClass
  public static void startServer() throws Exception {
    mockServer.startServer();
  }

  @AfterClass
  public static void stopServer() throws Exception {
    mockServer.stopServer();
  }

  @Test
  public void getGroupsShouldReturnOneGroup() {
    subject.setEnvironment(new Environment("http://localhost:8088", "key", "secret"));

    mockServer.addResponse("/rest/groups/@me", new ByteArrayResource((
        "{\"startIndex\":0,\"totalResults\":1,\"entry\":["
        + "{\"id\":{\"groupId\":\"urn:collab:group:surfteams.nl:nl:surfnet:diensten:bandwidth-on-demand\",\"type\":\"groupId\"},"
        + "\"title\":\"bandwidth-on-demand\",\"description\":\"The BoD development team\"}]"
        + ",\"itemsPerPage\":1}").getBytes()));

    Collection<Group> groups = subject.getGroups("urn:collab:person:surfguest.nl:alanvdam");

    assertThat(groups, hasSize(1));
    assertThat(groups.iterator().next().getTitle(), is("bandwidth-on-demand"));
  }
}

package nl.surfnet.bod.sabng;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

public class OfflineEntitlementsHandlerTest {

  private final EntitlementsHandler offlineEntitlementsHandler = new OfflineEntitlementsHandler();

  @Test
  public void shouldReturnOnlySURFnet() {
    List<String> instituteNames = offlineEntitlementsHandler.checkInstitutes(null);
    assertThat(instituteNames, contains("SURFNET"));

  }

}
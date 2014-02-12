package nl.surfnet.bod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.google.common.base.Optional;

public class AppComponentsTest {

  @Test
  public void testStunnelTranslationMap() throws Exception {
    AppComponents appComponents = new AppComponents();
    final Optional<Map<String,String>> map = appComponents.stunnelTranslationMap();

    assertTrue(map.isPresent());
    assertTrue(map.get().keySet().size() == 2);
    assertEquals(map.get().get("foo:9000"), "localhost:6789");
  }
}

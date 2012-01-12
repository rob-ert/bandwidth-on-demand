package nl.surfnet.bod.opendrac;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.Facility;

public class FacilityFactory {

  private final Logger log = LoggerFactory.getLogger(getClass());

  public Facility create(final String name, final String displayName) {
    final Map<String, String> map = new HashMap<String, String>();
    map.put("tna", displayName);
    map.put("pk", name);

    try {
      return new Facility(map);
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }

  }

}

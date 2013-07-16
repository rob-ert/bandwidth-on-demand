package nl.surfnet.bod.nbi.onecontrol.offline;

import nl.surfnet.bod.nbi.opendrac.NbiOpenDracOfflineClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("onecontrol-offline")
public class NbiOneControlOfflineClient extends NbiOpenDracOfflineClient {
}

package nl.surfnet.bod.nbi.onecontrol.offline;

import java.util.List;

import com.google.common.base.Optional;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.nbi.onecontrol.InventoryRetrievalClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType;

@Component
@Profile("onecontrol-offline")
public class InventoryRetrievalClientOffline implements InventoryRetrievalClient {

  @Override
  public List<PhysicalPort> getPhysicalPorts() {
    return null;
  }

  @Override
  public int getPhysicalPortCount() {
    return 0;
  }

  @Override
  public Optional<ServiceInventoryDataType.RfsList> getRfsInventory() {
    return null;
  }
}

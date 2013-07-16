package nl.surfnet.bod.nbi.onecontrol;

import java.util.List;

import com.google.common.base.Optional;
import nl.surfnet.bod.domain.PhysicalPort;
import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType;

public interface InventoryRetrievalClient {
  List<PhysicalPort> getPhysicalPorts();

  int getPhysicalPortCount();

  Optional<ServiceInventoryDataType.RfsList> getRfsInventory();
}

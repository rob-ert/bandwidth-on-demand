package nl.surfnet.bod.nbi.client;

import java.util.List;

import nl.surfnet.bod.nbi.client.generated.TerminationPoint;

public interface NbiClient {

	public List<TerminationPoint> getAllPorts();

}

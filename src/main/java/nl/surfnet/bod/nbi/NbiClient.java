package nl.surfnet.bod.nbi;

import java.util.List;

import nl.surfnet.bod.nbi.generated.TerminationPoint;

public interface NbiClient {

  /**
   *
   * @return A {@link List} of {@link TerminationPoint}'s or null if no ports
   *         were found.
   */
  List<TerminationPoint> findAllPorts();

}

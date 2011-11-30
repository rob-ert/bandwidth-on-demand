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

  /**
   *
   * @param name
   *          The name of the port
   * @return A {@link TerminationPoint} or <code>null</code> if nothing was
   *         found.
   */
  TerminationPoint findPortsByName(final String name);

}

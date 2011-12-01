package nl.surfnet.bod.nbi;

import nl.surfnet.bod.nbi.generated.PortDetail;
import nl.surfnet.bod.nbi.generated.TerminationPoint;

public class TerminationPointFactory {

  public TerminationPoint create(final String name, final String displayName) {
    TerminationPoint tpoint = new TerminationPoint();

    tpoint.setPortDetail(new PortDetail());
    tpoint.getPortDetail().setName(name);
    tpoint.getPortDetail().setDisplayName(displayName);

    return tpoint;
  }
}
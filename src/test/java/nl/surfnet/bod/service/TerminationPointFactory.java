package nl.surfnet.bod.service;

import nl.surfnet.bod.nbi.client.generated.PortDetail;
import nl.surfnet.bod.nbi.client.generated.TerminationPoint;

public class TerminationPointFactory {

    public TerminationPoint create(final String portId, final String name, final String displayName) {
        TerminationPoint tpoint = new TerminationPoint();

        tpoint.setPortDetail(new PortDetail());
        tpoint.getPortDetail().setName(name);
        tpoint.getPortDetail().setDisplayName(displayName);
        tpoint.getPortDetail().setPortId(portId);

        return tpoint;
    }
}
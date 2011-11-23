package nl.surfnet.bod.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.nbi.client.NbiClient;
import nl.surfnet.bod.nbi.client.generated.TerminationPoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * Ciena NorthBoundInterface implementation of the {@link PhysicalPortService}
 * 
 * @author Frank Mölder ($Author$)
 * @version $Revision$ $Date$
 */
@Service("physicalPortServiceNbiImpl")
public class PhysicalPortServiceNbiImpl implements PhysicalPortService {

    @Autowired
    private NbiClient nbiClient;

    @Override
    public List<PhysicalPort> findAll() {
        return transform(nbiClient.getAllPorts());
    }

    @Override
    public List<PhysicalPort> findEntries(final int firstResult, final int sizeNo) {
        return findAll();
    }

    @Override
    public long count() {
        long size = 0;

        List<TerminationPoint> ports = nbiClient.getAllPorts();
        if (!CollectionUtils.isEmpty(ports)) {
            size = ports.size();
        }

        return size;
    }

    @Override
    public void delete(final PhysicalPort physicalPort) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PhysicalPort find(final Long id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PhysicalPort find(final String portId) {

        return selectByPortId(nbiClient.getAllPorts(), portId);
    }

    @Override
    public void save(final PhysicalPort physicalPort) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PhysicalPort update(final PhysicalPort physicalPort) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Transforms a List of {@link TerminationPoint} to a List of
     * {@link PhysicalPort}
     * 
     * @param nbiPorts
     *            List of {@link TerminationPoint}
     * @return List of {@link PhysicalPort} which were transformed from the
     *         given {@link TerminationPoint}s. Empty list when param is null;
     */
    List<PhysicalPort> transform(final Collection<TerminationPoint> terminationPoints) {
        Collection<PhysicalPort> ports = null;
        List<PhysicalPort> portList = new ArrayList<PhysicalPort>();

        if (!CollectionUtils.isEmpty(terminationPoints)) {
            ports = Collections2.transform(terminationPoints, new Function<TerminationPoint, PhysicalPort>() {
                @Override
                public PhysicalPort apply(final TerminationPoint terminationPoint) {
                    return transform(terminationPoint);
                }

            });
        }

        if (!CollectionUtils.isEmpty(ports)) {
            portList = new ArrayList<PhysicalPort>(ports);
        }

        return portList;
    }

    /**
     * Selects a port with the specified portId from the given Collection.
     * 
     * @param terminationPoints
     *            Collection to search
     * @param portId
     *            PortId to select on
     * @return Matched instance or null when no match or multiple matches were
     *         found.
     */
    PhysicalPort selectByPortId(final Collection<TerminationPoint> terminationPoints, final String portId) {
        PhysicalPort result = null;
        Collection<TerminationPoint> filteredPorts = null;

        if (!CollectionUtils.isEmpty(terminationPoints) && (StringUtils.hasText(portId))) {
            filteredPorts = Collections2.filter(terminationPoints, new Predicate<TerminationPoint>() {

                @Override
                public boolean apply(final TerminationPoint port) {
                    boolean found = false;

                    if (port.getPortDetail() != null && portId.equals(port.getPortDetail().getPortId())) {
                        found = true;
                    }
                    return found;
                };
            });
        }

        if ((!CollectionUtils.isEmpty(filteredPorts)) && (filteredPorts.size() == 1)) {
            result = transform(filteredPorts.iterator().next());
        }

        return result;
    }

    /**
     * Transforms a {@link TerminationPoint} into a {@link PhysicalPort}
     * 
     * @param terminationPoint
     *            Object to transform
     * @return {@link PhysicalPort} transformed object
     */
    PhysicalPort transform(final TerminationPoint terminationPoint) {
        PhysicalPort physicalPort = new PhysicalPort();

        if (terminationPoint.getPortBasic() != null) {

        }

        if (terminationPoint.getPortDetail() != null) {
            physicalPort.setName(terminationPoint.getPortDetail().getName());
            physicalPort.setDisplayName(terminationPoint.getPortDetail().getDisplayName());
            physicalPort.setPortId(terminationPoint.getPortDetail().getPortId());
        }

        return physicalPort;
    }

}

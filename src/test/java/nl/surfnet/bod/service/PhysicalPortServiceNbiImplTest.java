package nl.surfnet.bod.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.nbi.client.generated.TerminationPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
public class PhysicalPortServiceNbiImplTest {

    private static final String PORT_ID_1 = "PortIdOne";
    private static final String PORT_ID_2 = "PortIdTwo";
    private static final String NAME_1 = "NameOne";
    private static final String NAME_2 = "NameTwo";
    private static final String DISPLAY_NAME_1 = "TestDisplayNameOne";
    private static final String DISPLAY_NAME_2 = "TestDisplayNameTwo";

    @Autowired
    private PhysicalPortServiceNbiImpl physicalPortServiceNbiImpl;

    @Test
    public void testMapTerminationPointToPhysicalPort() {
        // Setup
        TerminationPoint tp1 = new TerminationPointFactory().create(PORT_ID_1, NAME_1, DISPLAY_NAME_1);

        // Execute
        PhysicalPort physicalPort = physicalPortServiceNbiImpl.mapTerminationPointToPhysicalPort(tp1);

        // Verify
        assertEquals(PORT_ID_1, physicalPort.getPortId());
        assertEquals(NAME_1, physicalPort.getName());
        assertEquals(DISPLAY_NAME_1, physicalPort.getDisplayName());

    }

    @Test
    public void testMapTerminationPointToPhysicalPortNullArg() {

        // Execute
        PhysicalPort physicalPort = physicalPortServiceNbiImpl.mapTerminationPointToPhysicalPort(null);

        // Verify
        assertEquals(null, physicalPort.getPortId());
        assertEquals(null, physicalPort.getName());
        assertEquals(null, physicalPort.getDisplayName());

    }

    @Test
    public void testMapTerminationPointToPhysicalPortEmptyArg() {
        // Setup
        TerminationPoint tp = new TerminationPointFactory().create(null, null, null);
        // Execute
        PhysicalPort physicalPort = physicalPortServiceNbiImpl.mapTerminationPointToPhysicalPort(tp);

        // Verify
        assertEquals(null, physicalPort.getPortId());
        assertEquals(null, physicalPort.getName());
        assertEquals(null, physicalPort.getDisplayName());

    }

    @Test
    public void testTransform() {
        // Setup
        TerminationPoint tp1 = new TerminationPointFactory().create(PORT_ID_1, NAME_1, DISPLAY_NAME_1);
        TerminationPoint tp2 = new TerminationPointFactory().create(PORT_ID_2, NAME_2, DISPLAY_NAME_2);
        List<TerminationPoint> tpoints = Arrays.asList(tp1, tp2);

        // Execute
        Collection<PhysicalPort> transformedPorts = physicalPortServiceNbiImpl.transform(tpoints);

        // Verify
        assertEquals("size", 2, transformedPorts.size());
        Iterator<PhysicalPort> iterator = transformedPorts.iterator();

        PhysicalPort physicalPort = iterator.next();
        assertEquals("one", PORT_ID_1, physicalPort.getPortId());
        assertEquals("one", NAME_1, physicalPort.getName());
        assertEquals("one", DISPLAY_NAME_1, physicalPort.getDisplayName());

        physicalPort = iterator.next();
        assertEquals("two", PORT_ID_2, physicalPort.getPortId());
        assertEquals("two", NAME_2, physicalPort.getName());
        assertEquals("two", DISPLAY_NAME_2, physicalPort.getDisplayName());
    }

    @Test
    public void testTransformEmptyArg() {

        // Setup
        TerminationPoint tp1 = new TerminationPointFactory().create(null, null, null);
        List<TerminationPoint> tpoints = Arrays.asList(tp1);

        // Execute
        Collection<PhysicalPort> transformedPorts = physicalPortServiceNbiImpl.transform(tpoints);

        // Verify
        assertEquals("size", 1, transformedPorts.size());

        PhysicalPort physicalPort = transformedPorts.iterator().next();
        assertEquals("portId", null, physicalPort.getId());
        assertEquals("name", null, physicalPort.getName());
    }

    @Test
    public void testTransformNullArg() {

        // Execute
        List<PhysicalPort> ports = physicalPortServiceNbiImpl.transform(null);

        // Verify
        assertTrue(CollectionUtils.isEmpty(ports));
    }

    @Test
    public void testSelectByPortId() {

        TerminationPoint tp1 = new TerminationPointFactory().create(PORT_ID_1, NAME_1, DISPLAY_NAME_1);
        TerminationPoint tp2 = new TerminationPointFactory().create(PORT_ID_2, NAME_2, DISPLAY_NAME_2);
        List<TerminationPoint> tpoints = Arrays.asList(tp1, tp2);

        PhysicalPort selectedPort = physicalPortServiceNbiImpl.selectByPortId(tpoints, PORT_ID_1);
        assertEquals(PORT_ID_1, selectedPort.getPortId());
        assertEquals(NAME_1, selectedPort.getName());
        assertEquals(DISPLAY_NAME_1, selectedPort.getDisplayName());

    }

    @Test
    public void testSelectByPortIdNotFound() {

        TerminationPoint tp1 = new TerminationPointFactory().create(PORT_ID_1, NAME_1, DISPLAY_NAME_1);
        List<TerminationPoint> tpoints = Arrays.asList(tp1);

        PhysicalPort selectedPort = physicalPortServiceNbiImpl.selectByPortId(tpoints, PORT_ID_2);
        assertNull(selectedPort);

    }

    @Test
    public void testSelectByPortIdSearchNullArg() {

        TerminationPoint tp1 = new TerminationPointFactory().create(PORT_ID_1, NAME_1, DISPLAY_NAME_1);
        TerminationPoint tp2 = new TerminationPointFactory().create(PORT_ID_2, NAME_2, DISPLAY_NAME_2);
        List<TerminationPoint> tpoints = Arrays.asList(tp1, tp2);

        PhysicalPort selectedPort = physicalPortServiceNbiImpl.selectByPortId(tpoints, null);
        assertNull(selectedPort);

    }

    @Test
    public void testSelectByPortIdSearchEmptyArg() {

        TerminationPoint tp1 = new TerminationPointFactory().create(PORT_ID_1, NAME_1, DISPLAY_NAME_1);
        TerminationPoint tp2 = new TerminationPointFactory().create(PORT_ID_2, NAME_2, DISPLAY_NAME_2);
        List<TerminationPoint> tpoints = Arrays.asList(tp1, tp2);

        PhysicalPort selectedPort = physicalPortServiceNbiImpl.selectByPortId(tpoints, "");
        assertNull(selectedPort);

    }

    @Test
    public void testSelectByPortIdNullListArg() {

        PhysicalPort selectedPort = physicalPortServiceNbiImpl.selectByPortId(null, PORT_ID_1);
        assertNull(selectedPort);
    }

}

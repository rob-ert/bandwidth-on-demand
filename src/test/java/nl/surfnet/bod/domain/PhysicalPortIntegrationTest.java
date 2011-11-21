package nl.surfnet.bod.domain;

import static junit.framework.Assert.assertTrue;

import nl.surfnet.bod.domain.PhysicalPort;

import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = PhysicalPort.class)
public class PhysicalPortIntegrationTest {

	@Test
	public void testMarkerMethod() {
	}

	/**
	 * Override a testcase in the aspect by implementing it here.
	 */
	@Test
	public void testFindAllPhysicalPorts() {
		assertTrue(true);
	}

}

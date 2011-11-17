package org.surfnet.bod.domain;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = PhysicalResourceGroup.class)
public class PhysicalResourceGroupIntegrationTest {

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

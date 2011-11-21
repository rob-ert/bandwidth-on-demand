package nl.surfnet.bod.nbi.client;

import static org.junit.Assert.assertEquals;

import java.util.List;

import nl.surfnet.bod.nbi.client.generated.TerminationPoint;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx-nbi-client.xml" })
public class NbiClientTestIntegration {

	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	@Qualifier("nbiClient")
	private NbiClient nbiClient;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAllPortsWithDetails() {
		final List<TerminationPoint> allTerminationPoints = nbiClient.getAllPorts();
		assertEquals(260, allTerminationPoints.size());
	}

}

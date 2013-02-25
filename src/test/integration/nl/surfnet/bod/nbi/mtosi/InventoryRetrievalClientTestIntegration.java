/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.nbi.mtosi;

import static nl.surfnet.bod.util.TestHelper.devProperties;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.util.TestHelper.PropertiesEnvironment;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.tmforum.mtop.msi.wsdl.sir.v1_0.GetServiceInventoryException;
import org.tmforum.mtop.msi.xsd.sir.v1.GetServiceInventoryResponse;
import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType.RfsList;
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;

public class InventoryRetrievalClientTestIntegration {

  private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
  private InventoryRetrievalClient mtosiInventoryRetrievalLiveClient;

  @BeforeClass
  public static void printXml() {
    // Don't show full stack trace in soap result if an exception occurs
    System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
    System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump",
        "true");
    System.setProperty("com.sun.xml.ws.util.pipe.StandaloneTubeAssembler.dump",
        "true");
    System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump",
        "true");
  }

  @Before
  public void setup() {
    PropertiesEnvironment testEnv = devProperties();
    mtosiInventoryRetrievalLiveClient = new InventoryRetrievalClient(testEnv
        .getProperty("nbi.mtosi.inventory.retrieval.endpoint"));
  }

  @Test
  // @Ignore("No ports left after filtering on adminState and operationalState")
  public void getUnallocatedPorts() {
    final List<PhysicalPort> unallocatedPorts = mtosiInventoryRetrievalLiveClient.getUnallocatedPorts();
    assertThat(unallocatedPorts, hasSize(greaterThan(0)));
    final PhysicalPort firstPhysicalPort = unallocatedPorts.get(0);

    // It's always /rack=1/shelf=1 for every NE so we can use 1-1 safely
    assertThat(firstPhysicalPort.getBodPortId(), startsWith("SAP-"));
    assertThat(firstPhysicalPort.getNmsPortId(), containsString("1-1"));
    // fails for now ??
    // assertThat(firstPhysicalPort.getNmsPortSpeed(), notNullValue());
    assertThat(firstPhysicalPort.getNmsSapName(), startsWith("SAP-"));
    assertThat(firstPhysicalPort.getNmsSapName(), equalTo(firstPhysicalPort.getBodPortId()));
    assertThat(firstPhysicalPort.isAlignedWithNMS(), is(true));
  }

  @Test
  @Ignore("For troubleshooting")
  public void justPrintTheInventory() {
    RfsList rfsInventory = mtosiInventoryRetrievalLiveClient.getRfsInventory();

    for (ResourceFacingServiceType rfs : rfsInventory.getRfs()) {
      MtosiUtils.printDescribedByList(rfs.getDescribedByList());
    }
  }

  @Test
  @Ignore("No ports left after filtering on adminState and operationalState")
  public void getUnallocatedPortsCount() {
    assertThat(mtosiInventoryRetrievalLiveClient.getUnallocatedMtosiPortCount(), greaterThan(0));
  }

  @Test
  public void prettyPrintServiceInventoryXml() throws JAXBException, GetServiceInventoryException {
    GetServiceInventoryResponse inventory = mtosiInventoryRetrievalLiveClient.getServiceInventoryWithRfsFilter();

    logger.debug("ServiceInventoryWithRfsFilter: \n" + getPrettyPrintXml(inventory));
  }

  @Test
  public void prettyPrintSapList() throws JAXBException, GetServiceInventoryException {
    GetServiceInventoryResponse inventory = mtosiInventoryRetrievalLiveClient.getServiceInventoryWithSapFilter();

    logger.debug("ServiceInventoryWithSapFilter: \n  " + getPrettyPrintXml(inventory));
  }

  private <T> String getPrettyPrintXml(T xmlObject) throws JAXBException {
    final JAXBContext context = JAXBContext.newInstance(xmlObject.getClass());
    final Marshaller marshaller = context.createMarshaller();

    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    // Create a stringWriter to hold the XML
    final StringWriter stringWriter = new StringWriter();
    marshaller.marshal(xmlObject, stringWriter);

    return stringWriter.toString();
  }
}
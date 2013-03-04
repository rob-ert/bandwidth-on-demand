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
package nl.surfnet.bod;

import nl.surfnet.bod.support.SeleniumWithSingleSetup;

import org.junit.Test;

public class SearchAndSortTestSelenium extends SeleniumWithSingleSetup {

  @Override
  public void setupInitialData() {
    getNocDriver().createNewPhysicalResourceGroup(GROUP_SURFNET, ICT_MANAGERS_GROUP, "test@example.com");

    getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, "123NOC", "XYZPort", GROUP_SURFNET);
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, "987NOC", "ABDPort", GROUP_SURFNET);
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_3, "abcNOC", "abcPort", GROUP_SURFNET);
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_4, "bbcNOC", "xyzPort", GROUP_SURFNET);
  }

  @Test
  public void verifySearch() {
    String bodPortIdLabel1 = "Mock_ETH-1-1-1";
    String bodPortIdLabel2 = "Mock_ETH-1-2-3";
    String bodPortIdLabel3 = "Mock_OME0039_OC12-1-12-2";

    getNocDriver().addPhysicalPortToInstitute(GROUP_SURFNET, "NOC 1 pport label", "Mock_Poort 1de verdieping toren2");
    getNocDriver().addPhysicalPortToInstitute(GROUP_SURFNET, "NOC 2 pport label", "Mock_Poort 2de verdieping toren2");
    getNocDriver().addPhysicalPortToInstitute(GROUP_SURFNET, "NOC 3 pport label", "Mock_Poort 3de verdieping toren2");

    getNocDriver().verifyAllocatedPortsBySearch("label", bodPortIdLabel1, bodPortIdLabel2, bodPortIdLabel3);
    getNocDriver().verifyAllocatedPortsBySearch("*pport*", bodPortIdLabel1, bodPortIdLabel2, bodPortIdLabel3);

    getNocDriver().verifyAllocatedPortsBySearch("\"NOC 1 pport label\"", bodPortIdLabel1);
    getNocDriver().verifyAllocatedPortsBySearch("NOC ? pport", bodPortIdLabel1, bodPortIdLabel2, bodPortIdLabel3);
  }

  @Test
  public void verifySort() {
    getNocDriver().verifyAllocatedPortsBySort("nocLabel", "123NOC", "987NOC", "abcNOC", "bbcNOC");
  }

  @Test
  public void verifySearchAndSort() {
    getNocDriver().verifyAllocatedPortsBySearchAndSort("bcNO", "bodPortId", BOD_PORT_ID_4, BOD_PORT_ID_3);

    getNocDriver().verifyAllocatedPortsBySearchAndSort("ETH10G", "nocLabel", "123NOC", "987NOC");
  }

}
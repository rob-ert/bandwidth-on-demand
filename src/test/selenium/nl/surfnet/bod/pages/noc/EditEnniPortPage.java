/**
 * Copyright (c) 2012, 2013 SURFnet BV
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
package nl.surfnet.bod.pages.noc;

import nl.surfnet.bod.pages.AbstractFormPage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class EditEnniPortPage extends AbstractFormPage {

  @FindBy(id = "_nocLabel_id")
  private WebElement nocLabelInput;

  @FindBy(id = "_bodPortId_id")
  private WebElement bodPortIdInput;

  @FindBy(id = "_inboundPeer_id")
  private WebElement inboudPeerInput;

  @FindBy(id = "_outboundPeer_id")
  private WebElement outboundPeerInput;

  @FindBy(id = "_vlanRanges_id")
  private WebElement vlanRangesInput;

  public EditEnniPortPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static EditEnniPortPage get(RemoteWebDriver driver) {
    EditEnniPortPage page = new EditEnniPortPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void setNocLabel(String nocLabel) {
    clearAndSend(nocLabelInput, nocLabel);
  }

  public void setInboudPeer(String inboundPeer) {
    clearAndSend(inboudPeerInput, inboundPeer);
  }

  public void setOutboundPeer(String outboundPeer) {
    clearAndSend(outboundPeerInput, outboundPeer);
  }

  public void setVlanRange(String vlanRange) {
    clearAndSend(vlanRangesInput, vlanRange);
  }
}

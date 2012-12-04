/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.pages.user;

import nl.surfnet.bod.pages.AbstractFormPage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class RequestNewVirtualPortRequestPage extends AbstractFormPage {

  @FindBy(id = "message")
  private WebElement messageTextArea;

  @FindBy(id = "userLabel")
  private WebElement preferredLabel;

  @FindBy(id = "bandwidth")
  private WebElement bandwidthInput;

  public RequestNewVirtualPortRequestPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static RequestNewVirtualPortRequestPage get(RemoteWebDriver driver) {
    RequestNewVirtualPortRequestPage page = new RequestNewVirtualPortRequestPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendMessage(String message) {
    messageTextArea.clear();
    messageTextArea.sendKeys(message);
  }

  public void sentRequest() {
    save();
  }

  public void sendBandwidth(String bandwidth) {
    bandwidthInput.clear();
    bandwidthInput.sendKeys(bandwidth);
  }

  public void sendUserLabel(String userLabel) {
    preferredLabel.clear();
    preferredLabel.sendKeys(userLabel);
  }

}

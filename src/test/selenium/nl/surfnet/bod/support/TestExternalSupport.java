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
package nl.surfnet.bod.support;

import org.junit.Before;
import org.junit.Rule;

public abstract class TestExternalSupport {
  // Group names must match data in IddOfflineClient
  protected static final String GROUP_SURFNET = "SURFnet bv";
  protected static final String GROUP_SARA = "Stichting Algemeen Rekencentrum Achterveld";
  protected static final String GROUP_RUG = "Rijke Universiteit Gaanderen";

  protected static final String ICT_MANAGERS_GROUP =
    "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:selenium-ict-managers";
  protected static final String ICT_MANAGERS_GROUP_2 =
    "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:selenium-ict-managers2";

  protected static final String BOD_PORT_ID_1 = "Mock_ETH10G-1-13-1";
  protected static final String BOD_PORT_ID_2 = "Mock_ETH10G-1-13-2";
  protected static final String BOD_PORT_ID_3 = "Mock_ETH-1-13-5";
  protected static final String BOD_PORT_ID_4 = "Mock_ETH-1-13-4";

  protected static final String USERS_GROUP = "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:selenium-users";
  protected static final String NMS_PORT_ID_1 = "00-21-E1-D6-D6-70_ETH10G-1-13-1";
  protected static final String NMS_PORT_ID_2 = "00-21-E1-D6-D6-70_ETH10G-1-13-2";
  protected static final String NMS_PORT_ID_3 = "00-21-E1-D6-D5-DC_ETH-1-13-5";
  protected static final String NMS_PORT_ID_4 = "00-21-E1-D6-D5-DC_ETH-1-13-4";

  private static BodWebDriver webDriver = new BodWebDriver();

  @Rule
  public Screenshotter screenshotter = new Screenshotter(webDriver);

  @Before
  public final void initialize() {
    webDriver.initializeOnce();
  }

  protected BodWebDriver getWebDriver() {
    return webDriver;
  }

  protected BodUserWebDriver getUserDriver() {
    return webDriver.getUserDriver();
  }

  protected BodManagerWebDriver getManagerDriver() {
    return webDriver.getManagerDriver();
  }

  protected BodNocWebDriver getNocDriver() {
    return webDriver.getNocDriver();
  }

  protected BodAppManagerWebDriver getAppManagerDriver() {
    return webDriver.getAppManagerDriver();
  }

}

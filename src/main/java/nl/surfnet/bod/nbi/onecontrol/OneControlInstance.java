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
package nl.surfnet.bod.nbi.onecontrol;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "onecontrol", "onecontrol-offline" })
public class OneControlInstance {

  @Resource private OneControlConfiguration primaryConfiguration;
  @Resource private OneControlConfiguration secondaryConfiguration;

  private volatile OneControlConfiguration currentConfiguration;

  public OneControlInstance() {
  }

  @PostConstruct
  public void init() {
    currentConfiguration = primaryConfiguration;
  }

  public boolean isPrimaryEnabled() {
    return currentConfiguration == primaryConfiguration;
  }

  protected synchronized void switchConfiguration() {
    currentConfiguration = isPrimaryEnabled() ? secondaryConfiguration : primaryConfiguration;
  }

  public OneControlConfiguration getCurrentConfiguration() {
    return currentConfiguration;
  }

  public static class OneControlConfiguration {
    private final String inventoryRetrievalEndpoint;
    private final String serviceReserveEndpoint;
    private final String notificationProducerEndpoint;

    public OneControlConfiguration(String inventoryRetrievalEndpoint, String serviceReserveEndpoint, String notificationProducerEndpoint) {
      this.inventoryRetrievalEndpoint = inventoryRetrievalEndpoint;
      this.serviceReserveEndpoint = serviceReserveEndpoint;
      this.notificationProducerEndpoint = notificationProducerEndpoint;
    }

    public String getServiceReserveEndpoint() {
      return serviceReserveEndpoint;
    }

    public String getInventoryRetrievalEndpoint() {
      return inventoryRetrievalEndpoint;
    }

    public String getNotificationProducerEndpoint() {
      return notificationProducerEndpoint;
    }
  }
}

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.xml.ws.Holder;

import nl.surfnet.bod.nbi.onecontrol.HeaderBuilder;
import nl.surfnet.bod.nbi.onecontrol.OneControlInstance.OneControlConfiguration;

import org.joda.time.DateTime;
import org.junit.Test;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;


public class HeaderBuilderTest {

  @Test
  public void reserveHeaderShouldFileHeaders() {
    final String endpoint = "http://nonexisting.example.com/wsendpoint";

    Holder<Header> holder = HeaderBuilder.buildReserveHeader(new OneControlConfiguration("wrong", endpoint, "wrong"));

    Header header = holder.value;

    assertThat(header.getDestinationURI(), is(endpoint));
    assertThat(header.getActivityName(), is("reserve"));
    assertThat(header.getTimestamp().getYear(), is(DateTime.now().getYear()));
  }

  @Test
  public void inventoryHeaderShouldFileHeaders() {
    final String endpoint = "http://nonexisting.example.com/wsendpoint";

    Holder<Header> holder = HeaderBuilder.buildInventoryHeader(new OneControlConfiguration(endpoint, "wrong", "wrong"));

    Header header = holder.value;

    assertThat(header.getDestinationURI(), is(endpoint));
    assertThat(header.getActivityName(), is("getServiceInventory"));
    assertThat(header.getTimestamp().getYear(), is(DateTime.now().getYear()));
  }

  @Test
  public void notificationProducerHeader() {
    final String endpoint = "http://nonexisting.example.com/wsendpoint";

    Holder<Header> holder = HeaderBuilder.buildNotificationHeader(new OneControlConfiguration("wrong", "wrong", endpoint));

    Header header = holder.value;

    assertThat(header.getDestinationURI(), is(endpoint));
    assertThat(header.getActivityName(), is("subscribe"));
    assertThat(header.getTimestamp().getYear(), is(DateTime.now().getYear()));
  }
}

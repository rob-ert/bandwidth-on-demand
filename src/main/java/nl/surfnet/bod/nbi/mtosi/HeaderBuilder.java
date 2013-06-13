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
package nl.surfnet.bod.nbi.mtosi;

import javax.xml.ws.Holder;

import nl.surfnet.bod.util.XmlUtils;

import org.joda.time.DateTime;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationPatternType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationStyleType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.hdr.v1.MessageTypeType;

public final class HeaderBuilder {

  private HeaderBuilder() {
  }

  private static Holder<Header> buildHeader(String endPoint, String activityName, String msgName) {
    final Header header = new Header();
    header.setDestinationURI(endPoint);
    header.setCommunicationStyle(CommunicationStyleType.RPC);
    header.setCommunicationPattern(CommunicationPatternType.SIMPLE_RESPONSE);
    header.setTimestamp(XmlUtils.toGregorianCalendar(DateTime.now()));
    header.setActivityName(activityName);
    header.setMsgName(msgName);
    // TODO should change sender URI?
    header.setSenderURI("http://localhost:9009");
    header.setMsgType(MessageTypeType.REQUEST);

    return new Holder<Header>(header);
  }

  public static Holder<Header> buildReserveHeader(String endPoint) {
    return buildHeader(endPoint, "reserve", "reserveRequest");
  }

  public static Holder<Header> buildInventoryHeader(String endPoint) {
    return buildHeader(endPoint, "getServiceInventory", "getServiceInventoryRequest");
  }

  public static Holder<Header> buildNotificationHeader(String endPoint) {
    return buildHeader(endPoint, "subscribe", "subscribeRequest");
  }

}

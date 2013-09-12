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
package nl.surfnet.bod.nsi;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.NsiVersion;
import nl.surfnet.bod.domain.ReservationEndPoint;
import nl.surfnet.bod.domain.VirtualPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NsiHelper {

  // Matches OPAQUE-PART of OGF URN (GFD.202, see https://www.gridforum.org/documents/GFD.202.pdf).
  private static final String GFD_202_OPAQUE_PART_PATTERN = "[a-zA-Z0-9+,\\-.:;=_!$()*@~&]*";
  public static final String NURN_PATTERN_REGEXP = "urn:ogf:network:[a-zA-Z0-9\\-.]+:[0-9]{4,8}:" + GFD_202_OPAQUE_PART_PATTERN + "(\\?" +  GFD_202_OPAQUE_PART_PATTERN + ")?" + "(#" +  GFD_202_OPAQUE_PART_PATTERN + ")?";
  private static final Pattern NURN_PATTERN = Pattern.compile(NURN_PATTERN_REGEXP);

  private static final String URN_OGF = "urn:ogf:network";
  private static final Joiner URN_JOINER = Joiner.on(":");

  private final String networkIdV1;
  private final String networkIdV2;
  private final String urnGlobalReservationId;
  private final String urnStpV1;
  private final String urnStpV2;
  private final String providerId;

  private final Pattern stpPatternV1;
  private final Pattern stpPatternV2;

  @Autowired
  public NsiHelper(
      @Value("${nsi.v1.networkId}") String networkIdV1,
      @Value("${nsi.v2.networkId}") String networkIdV2,
      @Value("${nsi.v2.providerId}") String providerId,
      @Value("${nsi.v2.topologyId}") String topologyId,
      @Value("${nsi.globalReservationId}") String urnGlobalReservationId) {

    this.networkIdV1 = networkIdV1;
    this.networkIdV2 = networkIdV2;
    this.urnGlobalReservationId = urnGlobalReservationId;
    this.urnStpV1 = join(URN_OGF, "stp", networkIdV1);
    this.urnStpV2 = join(URN_OGF,  networkIdV2, topologyId);
    this.providerId = providerId;

    this.stpPatternV1 = Pattern.compile(urnStpV1 + ":([0-9]+)");
    this.stpPatternV2 = Pattern.compile(urnStpV2 + ":(" + GFD_202_OPAQUE_PART_PATTERN + ")");
  }

  public String getNetworkIdV1() {
    return networkIdV1;
  }

  public String getNetworkIdV2() {
    return networkIdV2;
  }

  public String parseLocalNsiId(String stpId, NsiVersion nsiVersion) {
    Pattern pattern = nsiVersion == NsiVersion.ONE ? stpPatternV1 : stpPatternV2;
    Matcher matcher = pattern.matcher(stpId);

    if (!matcher.matches()) {
      return null;
    }

    return matcher.group(1);
  }

  public boolean isValidNurn(final String candidate) {
    return NURN_PATTERN.matcher(candidate).matches();
  }

  public String generateGlobalReservationId() {
    return join(urnGlobalReservationId, UUID.randomUUID().toString());
  }

  public String getStpIdV1(VirtualPort vp) {
    return join(urnStpV1, vp.getId());
  }

  public String getStpIdV2(VirtualPort vp) {
    return join(urnStpV2, vp.getId());
  }

  public String getStpIdV2(EnniPort port) {
    return join(urnStpV2, port.getBodPortId());
  }

  public String getStpIdV1(EnniPort port) {
    return join(urnStpV1, port.getBodPortId());
  }

  public String getStpIdV1(ReservationEndPoint endPoint) {
    return endPoint.getVirtualPort().transform(new Function<VirtualPort, String>() {
      public String apply(VirtualPort port) {
        return getStpIdV1(port);
      }
    }).or(getStpIdV1(endPoint.getEnniPort().get()));
  }

  public String getStpIdV2(ReservationEndPoint endPoint) {
    return endPoint.getVirtualPort().transform(new Function<VirtualPort, String>() {
      public String apply(VirtualPort port) {
        return getStpIdV2(port);
      }
    }).or(getStpIdV2(endPoint.getEnniPort().get()));
  }

  public static String generateConnectionId() {
    return UUID.randomUUID().toString();
  }

  public static String generateCorrelationId() {
    return join("urn:uuid", UUID.randomUUID().toString());
  }

  public String getProviderNsaV2() {
    return join(URN_OGF, networkIdV2, providerId);
  }

  public String getProviderNsaV1() {
    return join(URN_OGF, "nsa", networkIdV1);
  }

  public String getUrnStpV2() {
    return urnStpV2;
  }

  private static String join(Object... parts) {
    return URN_JOINER.join(parts);
  }

}
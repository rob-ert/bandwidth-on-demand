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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.surfnet.bod.domain.NsiVersion;

public class NsiConstants {

  public static String URN_OGF = "urn:ogf:network";
  public static String NETWORK_ID = "surfnet.nl";
  public static String URN_PROVIDER_NSA = URN_OGF + ":nsa:" + NETWORK_ID;
  public static String URN_STP_V1 = URN_OGF + ":stp:" + NETWORK_ID;
  public static String URN_STP_V2 = URN_OGF + ":" + NETWORK_ID + ":1990";
  public static String URN_GLOBAL_RESERVATION_ID = "urn:nl:surfnet:diensten:bod";
  public static Pattern NSIV2_STP_PATTERN = Pattern.compile(URN_STP_V2 + ":([0-9]+)");
  public static Pattern NSIV1_STP_PATTERN = Pattern.compile(URN_STP_V1 + ":([0-9]+)");

  public static String parseLocalNsiId(String stpId, NsiVersion nsiVersion) {
    Pattern pattern = nsiVersion == NsiVersion.ONE ? NSIV1_STP_PATTERN : NSIV2_STP_PATTERN;
    Matcher matcher = pattern.matcher(stpId);

    if (!matcher.matches()) {
      return null;
    }
    return matcher.group(1);
  }
}

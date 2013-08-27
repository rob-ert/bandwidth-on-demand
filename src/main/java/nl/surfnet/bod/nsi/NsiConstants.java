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

public final class NsiConstants {

  public static final String URN_OGF = "urn:ogf:network";

  public static final String NETWORK_ID_V1 = "surfnet.nl";
  public static final String NETWORK_ID_V2 = "surfnet.nl:1990";

  public static final String URN_PROVIDER_NSA_V1 = URN_OGF + ":nsa:" + NETWORK_ID_V1;
  public static final String URN_PROVIDER_NSA_V2 = URN_OGF + ":nsa:" + NETWORK_ID_V2;

  public static final String URN_STP_V1 = URN_OGF + ":stp:" + NETWORK_ID_V1;
  public static final String URN_STP_V2 = URN_OGF + ":" + NETWORK_ID_V2;
  public static final String URN_GLOBAL_RESERVATION_ID = "urn:nl:surfnet:diensten:bod";

  // Matches OPAQUE-PART of OGF URN (GFD.202, see https://www.gridforum.org/documents/GFD.202.pdf).
  public static final String GFD_202_OPAQUE_PART_PATTERN = "[a-zA-Z0-9+,\\-.:;=_!$()*@~&]*";
  public static final String NURN_PATTERN_REGEXP = "urn:ogf:network:[a-zA-Z0-9\\-.]+:[0-9]{4,8}:" + GFD_202_OPAQUE_PART_PATTERN + "(\\?" +  GFD_202_OPAQUE_PART_PATTERN + ")?" + "(#" +  GFD_202_OPAQUE_PART_PATTERN + ")?";

  public static final Pattern NURN_PATTERN = Pattern.compile(NURN_PATTERN_REGEXP);

  public static final String NSIV2_STP_PATTERN_REGEXP = URN_STP_V2 + ":(" + GFD_202_OPAQUE_PART_PATTERN + ")";

  public static final Pattern NSIV1_STP_PATTERN = Pattern.compile(URN_STP_V1 + ":([0-9]+)");
  public static final Pattern NSIV2_STP_PATTERN = Pattern.compile(NSIV2_STP_PATTERN_REGEXP);

  public static final String parseLocalNsiId(String stpId, NsiVersion nsiVersion) {
    Pattern pattern = nsiVersion == NsiVersion.ONE ? NSIV1_STP_PATTERN : NSIV2_STP_PATTERN;
    Matcher matcher = pattern.matcher(stpId);

    if (!matcher.matches()) {
      return null;
    }
    return matcher.group(1);
  }

  public static boolean isValidNurn(final String candidate) {
    return NURN_PATTERN.matcher(candidate).matches();
  }
}

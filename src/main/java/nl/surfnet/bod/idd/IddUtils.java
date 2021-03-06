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
package nl.surfnet.bod.idd;

import java.util.Collection;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.idd.generated.Klanten;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

final class IddUtils {

  private IddUtils() {
  }

  static Collection<Institute> transformKlanten(Collection<Klanten> klanten, final boolean alignedWithIDD) {
    Collection<Optional<Institute>> institutes = Collections2.transform(klanten,
        new Function<Klanten, Optional<Institute>>() {
          @Override
          public Optional<Institute> apply(Klanten klant) {
            return transformKlant(klant, alignedWithIDD);
          }
        });

    return Lists.newArrayList(Optional.presentInstances(institutes));
  }

  static Optional<Institute> transformKlant(Klanten klant, boolean alignedWithIDD) {
    if (Strings.isNullOrEmpty(klant.getKlantnaam()) && Strings.isNullOrEmpty(klant.getKlantafkorting())) {
      return Optional.absent();
    }

    return Optional.of(new Institute(Long.valueOf(klant.getKlant_id()), trimIfNotNull(klant.getKlantnaam()),
        trimIfNotNull(klant.getKlantafkorting()), alignedWithIDD));
  }

  private static String trimIfNotNull(String value) {
    return value != null ? value.trim() : value;
  }

}

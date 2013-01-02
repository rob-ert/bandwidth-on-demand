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
package nl.surfnet.bod.idd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Collection;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.support.KlantenFactory;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;


public class IddUtilsTest {

  @Test
  public void bothNaamAndAfkortingAreEmptyShouldGiveNull() {
    Klanten klant = new KlantenFactory().setKlantnaam("").setKlantafkorting(null).create();

    Optional<Institute> institute = IddUtils.transformKlant(klant, true);

    assertThat(institute.isPresent(), is(false));
  }

  @Test
  public void transformKlantWithoutAfkorting() {
    Klanten klant = new KlantenFactory().setKlantnaam(" SURF ").setKlantafkorting(null).create();

    Optional<Institute> institute = IddUtils.transformKlant(klant, true);

    assertThat(institute.get().getName(), is("SURF"));
  }

  @Test
  public void transformKlantWithoutNaam() {
    Klanten klant = new KlantenFactory().setKlantnaam(null).setKlantafkorting(" UU ").create();

    Optional<Institute> institute = IddUtils.transformKlant(klant, true);

    assertThat(institute.get().getShortName(), is("UU"));
  }

  @Test
  public void transformKlantWithId() {
    Klanten klant = new KlantenFactory().setKlantnaam("Universiteit Utrecht").setKlantafkorting("UU").setKlantid(1)
        .create();

    Optional<Institute> institute = IddUtils.transformKlant(klant, true);

    assertThat(institute.get().isAlignedWithIDD(), is(true));
    assertThat(institute.get().getId(), is(1L));
    assertThat(institute.get().getShortName(), is("UU"));
  }

  @Test
  public void transformKlantenShouldLeaveOutNulls() {
    Klanten klant = new KlantenFactory().setKlantnaam("Universiteit Utrecht").setKlantafkorting("UU").setKlantid(1)
        .create();
    Klanten klantWithNullValues = new KlantenFactory().setKlantnaam(null).setKlantafkorting(null).setKlantid(2)
        .create();

    Collection<Institute> klanten = IddUtils.transformKlanten(ImmutableList.of(klant, klantWithNullValues), true);

    assertThat(klanten, hasSize(1));
  }
}

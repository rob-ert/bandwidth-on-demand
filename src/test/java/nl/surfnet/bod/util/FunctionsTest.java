/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.util;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Collection;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.support.KlantenFactory;

public class FunctionsTest {

  @Test
  public void bothNaamAndAfkortingAreEmptyShouldGiveNull() {
    Klanten klant = new KlantenFactory().setKlantnaam("").setKlantafkorting(null).create();

    Optional<Institute> institute = Functions.transformKlant(klant, true);

    assertThat(institute.isPresent(), is(false));
  }

  @Test
  public void transformKlantWithoutAfkorting() {
    Klanten klant = new KlantenFactory().setKlantnaam(" SURF ").setKlantafkorting(null).create();

    Optional<Institute> institute = Functions.transformKlant(klant, true);

    assertThat(institute.get().getName(), is("SURF"));
  }

  @Test
  public void transformKlantWithoutNaam() {
    Klanten klant = new KlantenFactory().setKlantnaam(null).setKlantafkorting(" UU ").create();

    Optional<Institute> institute = Functions.transformKlant(klant, true);

    assertThat(institute.get().getShortName(), is("UU"));
  }

  @Test
  public void transformKlantWithId() {
    Klanten klant = new KlantenFactory().setKlantnaam("Universiteit Utrecht").setKlantafkorting("UU").setKlantid(1)
        .create();

    Optional<Institute> institute = Functions.transformKlant(klant, true);

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

    Collection<Institute> klanten = Functions.transformKlanten(ImmutableList.of(klant, klantWithNullValues), true);

    assertThat(klanten, hasSize(1));
  }

}

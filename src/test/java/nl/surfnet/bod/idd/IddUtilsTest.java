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

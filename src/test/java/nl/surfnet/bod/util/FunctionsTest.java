package nl.surfnet.bod.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.support.KlantenFactory;

import org.junit.Test;

public class FunctionsTest {

  @Test
  public void bothNaamAndAfkortingAreEmptyShouldGiveNull() {
    Klanten klant = new KlantenFactory().setKlantnaam("").setKlantafkoring(null).create();

    Institute institute = Functions.transformKlant(klant, true);

    assertThat(institute, nullValue());
  }

  @Test
  public void transformKlantWithoutAfkorting() {
    Klanten klant = new KlantenFactory().setKlantnaam(" SURF ").setKlantafkoring(null).create();

    Institute institute = Functions.transformKlant(klant, true);

    assertThat(institute.getName(), is("SURF"));
  }

  @Test
  public void transformKlantWithoutNaam() {
    Klanten klant = new KlantenFactory().setKlantnaam(null).setKlantafkoring(" UU ").create();

    Institute institute = Functions.transformKlant(klant, true);

    assertThat(institute.getShortName(), is("UU"));
  }

  @Test
  public void transformKlantWithId() {
    Klanten klant = new KlantenFactory().setKlantnaam("Universiteit Utrecht").setKlantafkoring("UU").setKlantid(1)
        .create();

    Institute institute = Functions.transformKlant(klant, true);

    assertThat(institute.isAlignedWithIDD(), is(true));
    assertThat(institute.getId(), is(1L));
    assertThat(institute.getShortName(), is("UU"));
  }

}

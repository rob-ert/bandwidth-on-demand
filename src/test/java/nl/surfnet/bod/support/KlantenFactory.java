package nl.surfnet.bod.support;

import nl.surfnet.bod.idd.generated.Klanten;

public class KlantenFactory {

  private String klantnaam;
  private String klantafkorting;
  private int klantid;

  public Klanten create() {
    Klanten result = new Klanten();

    result.setKlant_id(klantid);
    result.setKlantnaam(klantnaam);
    result.setKlantafkorting(klantafkorting);

    return result;
  }

  public KlantenFactory setKlantid(int id) {
    this.klantid = id;
    return this;
  }
  public KlantenFactory setKlantnaam(String naam) {
    this.klantnaam = naam;
    return this;
  }

  public KlantenFactory setKlantafkoring(String afkorting) {
    this.klantafkorting = afkorting;
    return this;
  }
}

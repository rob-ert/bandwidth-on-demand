package nl.surfnet.bod.idd;

import java.util.Collection;

import nl.surfnet.bod.idd.generated.Klanten;

public interface IddClient {

  Collection<Klanten> getKlanten();

}

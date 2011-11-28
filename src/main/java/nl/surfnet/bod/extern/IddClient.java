package nl.surfnet.bod.extern;

import java.util.Collection;

import nl.surfnet.bod.idd.Klanten;

public interface IddClient {

    Collection<Klanten> getKlanten();

}

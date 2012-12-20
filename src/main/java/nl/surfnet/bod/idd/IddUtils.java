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

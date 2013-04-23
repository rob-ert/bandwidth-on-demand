package nl.surfnet.bod.nsi;

import static nl.surfnet.bod.nsi.NsiConstants.URN_GLOBAL_RESERVATION_ID;

import java.util.UUID;

public final class NsiHelper {
  private NsiHelper() {
  }

  public static String generateGlobalReservationId() {
    return URN_GLOBAL_RESERVATION_ID + ":" + UUID.randomUUID().toString();
  }

  public static String generateConnectionId() {
    return UUID.randomUUID().toString();
  }

}

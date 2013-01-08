package nl.surfnet.bod.nbi.mtosi;


public final class MtosiUtils {

  private static final String PTP_FORMAT = "/rack=%s/shelf=%s/slot=%s/port=%s";
  private static final String PTP_WITH_SUB_SLOT_FORMAT = "/rack=%s/shelf=%s/slot=%s/sub_slot=%s/port=%s";

  private MtosiUtils() {
  }

  public static String physicalTerminationPointToNmsPortId(String ptp) {
    return ptp.replace("rack=", "").replace("shelf=", "").replace("sub_slot=", "").replace("slot=", "")
        .replace("port=", "").replaceFirst("/", "").replaceAll("/", "-");
  }

  public static String nmsPortIdToPhysicalTerminationPoint(String nmsPortId) {
    Object[] parts = nmsPortId.split("-");
    if (parts.length == 4) {
      return String.format(PTP_FORMAT, parts);
    }
    else if (parts.length == 5) {
      return String.format(PTP_WITH_SUB_SLOT_FORMAT, parts);
    }
    else {
      throw new IllegalArgumentException("The nmsPortId can not be converted to a ptp");
    }
  }

}

package nl.surfnet.bod.nsi.ws;

public class ConnectionServiceErrorCodes {

  public enum PAYLOAD {
    PAYLOAD_ERROR("00100", ""), MISSING_PARAMETER("00101", ""), NOT_IMPLEMENTED("00102", "");

    final String id, text;

    PAYLOAD(final String id, final String text) {
      this.id = id;
      this.text = text;
    }

    public final String getId() {
      return id;
    }

    public final String getText() {
      return text;
    }
  }

  public enum CONNECTION {
    CONNECTION_ERROR("00200", ""), INVALID_TRANSITION("00201", ""), CONNECTION_EXISTS("00202", ""), CONNECTION_NONEXISTENT(
        "00203", ""), CONNECTION_GONE("00204", "");
    final String id, text;

    CONNECTION(final String id, final String text) {
      this.id = id;
      this.text = text;
    }

    public final String getId() {
      return id;
    }

    public final String getText() {
      return text;
    }
  }

  public enum SECURITY {
    SECURITY_ERROR("00300", ""), UNAUTHORIZED("00301", "");
    final String id, text;

    SECURITY(final String id, final String text) {
      this.id = id;
      this.text = text;
    }

    public final String getId() {
      return id;
    }

    public final String getText() {
      return text;
    }
  }

  public enum TOPOLOGY {
    TOPOLOGY_ERROR("00400", ""), UNKNOWN_STP("00401", ""), STP_RESOLUTION_ERROR("00402", ""), NO_PATH_FOUND("00403", "");
    final String id, text;

    TOPOLOGY(final String id, final String text) {
      this.id = id;
      this.text = text;
    }

    public final String getId() {
      return id;
    }

    public final String getText() {
      return text;
    }
  }

  public enum INTERNAL {
    INTERNAL_ERROR("00400", ""), INTERNAL_NRM_ERROR("00401", "");
    final String id, text;

    INTERNAL(final String id, final String text) {
      this.id = id;
      this.text = text;
    }

    public final String getId() {
      return id;
    }

    public final String getText() {
      return text;
    }
  }

  public enum RESOURCE {
    RESOURCE_UNAVAILABLE("00600", ""), STP_UNAVALABLE("00601", ""), BANDWIDTH_UNAVAILABLE("00602", "");
    final String id, text;

    RESOURCE(final String id, final String text) {
      this.id = id;
      this.text = text;
    }

    public final String getId() {
      return id;
    }

    public final String getText() {
      return text;
    }
  }

}

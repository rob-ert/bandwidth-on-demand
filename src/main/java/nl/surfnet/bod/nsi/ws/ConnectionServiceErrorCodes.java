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
package nl.surfnet.bod.nsi.ws;

public class ConnectionServiceErrorCodes {

  public enum PAYLOAD {
    PAYLOAD_ERROR("00100", ""), MISSING_PARAMETER("00101", ""), NOT_IMPLEMENTED("00102", "");

    private final String id, text;

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
    CONNECTION_ERROR("00200", ""), INVALID_TRANSITION("00201", ""), CONNECTION_EXISTS("00202", ""),
    CONNECTION_NONEXISTENT("00203", ""), CONNECTION_GONE("00204", "");

    private final String id, text;

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

    private final String id, text;

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

    private final String id, text;

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

    private final String id, text;

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

    private final String id, text;

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

/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.nsi;

public class ConnectionServiceProviderErrorCodes {

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
    SECURITY_ERROR("00300", ""),
    UNAUTHORIZED("00301", ""),
    MISSING_GRANTED_SCOPE("00302", "Access token is not valid for current scope");

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

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

public enum ConnectionServiceProviderError {

  PAYLOAD_ERROR("100", "PAYLOAD_ERROR", ""),
  MISSING_PARAMETER("101", "MISSING_PARAMETER", "Invalid or missing parameter"),
  UNSUPPORTED_PARAMETER("102", "UNSUPPORTED_PARAMETER", "Parameter provided contains an unsupported value which MUST be processed"),
  NOT_IMPLEMENTED("103", "NOT_IMPLEMENTED", "This operation is not implemented yet"),
  VERSION_NOT_SUPPORTED("104", "VERSION_NOT_SUPPORTED", "The service version requested in NSI header is not supported"),

  CONNECTION_ERROR("200", "", ""),
  INVALID_TRANSITION("201", "INVALID_TRANSITION", "Connection state machine is in invalid state for received message"),
  CONNECTION_EXISTS("202", "CONNECTION_EXISTS", "Schedule already exists for connectionId"),
  CONNECTION_NON_EXISTENT("203", "CONNECTION_NONEXISTENT", "Schedule does not exist for connectionId"),
  CONNECTION_GONE("204", "CONNECTION_GONE", ""),
  CONNECTION_CREATE_ERROR("205", "CONNECTION_CREATE_ERROR", "Failed to create connection (payload was ok, something went wrong)"),

  SECURITY_ERROR("300", "SECURITY_ERROR", ""),
  AUTHENTICATION_FAILURE("301", "AUTHENTICATION_FAILURE", ""),
  UNAUTHORIZED("302", "UNAUTHORIZED", ""),

  TOPOLOGY_ERROR("400", "TOPOLOGY_ERROR", ""),
  UNKNOWN_STP("401", "UNKNOWN_STP", "Could not find STP in topology database"),
  STP_RESOLUTION_ERROR("402", "STP_RESOLUTION_ERROR", "Could not resolve STP to a managing NSA"),
  NO_PATH_FOUND("403", "NO_PATH_FOUND", "Path computation failed to resolve route for reservation"),
  VLAN_ID_INTERCHANGE_NOT_SUPPORTED("404", "VLANID_INTERCHANGE_NOT_SUPPORTED", "VlanId interchange not supported for requested path"),

  INTERNAL_ERROR("500", "INTERNAL_ERROR", "An internal error has caused a message processing failure"),
  INTERNAL_NRM_ERROR("501", "INTERNAL_NRM_ERROR", "An internal NRM error has caused a message processing failure"),

  RESOURCE_UNAVAILABLE("600", "RESOURCE_UNAVAILABLE", ""),
  STP_UNAVAILABLE("601", "STP_UNAVAILABLE", "Specified STP already in use"),
  BANDWIDTH_UNAVAILABLE("602", "BANDWIDTH_UNAVAILABLE", "Insufficient bandwdith available for reservation");

  private final String errorId;
  private final String code;
  private final String message;

  private ConnectionServiceProviderError(String errorId, String code, String message) {
    this.errorId = errorId;
    this.code = code;
    this.message = message;
  }

  public String getErrorId() {
    return errorId;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

}

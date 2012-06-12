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
package nl.surfnet.bod.nsi.ws.v1sc;

import javax.jws.WebService;
import javax.xml.ws.Holder;

import org.ogf.schemas.nsi._2011._10.connection._interface.ForcedEndRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
import org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveConfirmedType;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import nl.surfnet.bod.nsi.ws.ConnectionService;

@Service("nsiRequester_v1_sc")
@WebService(serviceName = "ConnectionServiceRequester",
    portName = "ConnectionServiceRequesterPort",
    endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort",
    targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/requester",
    wsdlLocation = "/WEB-INF/wsdl/nsi/ogf_nsi_connection_requester_v1_0.wsdl")
public class ConnectionServiceRequester extends ConnectionService {

  private final Logger log = getLog();

  public void reserveConfirmed(Holder<String> correlationId, ReserveConfirmedType reserveConfirmed)
      throws ServiceException {

    // Validate we received the confirmed message.

    // Build an internal request for this provisionConfirmed request.

    /*
     * Break out the attributes we need for handling.
     */

    /*
     * Extract NSA fields. These are currently named incorrectly as the
     * specification states they should contain the NsNetwork name. Try not to
     * get confused when looking up information in topology using this field.
     */

    /*
     * Verify that this message was targeting this NSA by looking at the
     * ProviderNSA field. If invalid we will throw an exception.
     */

    /*
     * Get the connectionId from the request as this is used as our primary
     * index.
     */

    // Route this message to the appropriate actor for processing.

  }

  public void reserveFailed(Holder<String> correlationId, GenericFailedType reserveFailed) throws ServiceException {

    log.info("Reservation failed received for correlationid {}", correlationId.value);

    // Validate we received the confirmed message.

    // Build an internal request for this reserveFailed request.

    /*
     * Break out the attributes we need for handling.
     */

    /*
     * Extract NSA fields. These are currently named incorrectly as the
     * specification states they should contain the NsNetwork name. Try not to
     * get confused when looking up information in topology using this field.
     */

    /*
     * Verify that this message was targeting this NSA by looking at the
     * ProviderNSA field. If invalid we will throw an exception.
     */

    /*
     * Get the connectionId from the request as this is used as our primary
     * index.
     */

    // Route this message to the appropriate actor for processing.

  }

  /**
   *
   * @param correlationId
   *          The correlation for the provision operation we issued to the child
   *          NSA.
   * @param provisionConfirmed
   *          The provsionConfirmed NSI message indicating that the
   *          corresponding child connection segment has been provisioned.
   * @return correlationId is used to populate the ACK message.
   * @throws ServiceException
   */
  public void provisionConfirmed(Holder<String> correlationId, GenericConfirmedType provisionConfirmed)
      throws ServiceException {

    // Validate we received the confirmed message.
    // Build an internal request for this provisionConfirmed request.

    /*
     * Break out the attributes we need for handling.
     */

    /*
     * Extract NSA fields. These are currently named incorrectly as the
     * specification states they should contain the NsNetwork name. Try not to
     * get confused when looking up information in topology using this field.
     */

    /*
     * Verify that this message was targeting this NSA by looking at the
     * ProviderNSA field. If invalid we will throw an exception.
     */

    /*
     * Get the connectionId from the request as this is used as our primary
     * index.
     */

    // Route this message to the appropriate actor for processing.

  }

  public void provisionFailed(Holder<String> correlationId, GenericFailedType provisionFailed) throws ServiceException {
    // Validate we received the confirmed message.

    // Build an internal request for this provisionFailed request.

    /*
     * Break out the attributes we need for handling.
     */

    /*
     * Extract NSA fields. These are currently named incorrectly as the
     * specification states they should contain the NsNetwork name. Try not to
     * get confused when looking up information in topology using this field.
     */

    /*
     * Verify that this message was targeting this NSA by looking at the
     * ProviderNSA field. If invalid we will throw an exception.
     */

    /*
     * Get the connectionId from the request as this is used as our primary
     * index.
     */

    // Route this message to the appropriate actor for processing.

  }

  public void releaseConfirmed(Holder<String> correlationId, GenericConfirmedType releaseConfirmed)
      throws ServiceException {

    // Validate we received the confirmed message.

    // Build an internal request for this releaseConfirmed request.

    /*
     * Break out the attributes we need for handling.
     */

    /*
     * Extract NSA fields. These are currently named incorrectly as the
     * specification states they should contain the NsNetwork name. Try not to
     * get confused when looking up information in topology using this field.
     */

    /*
     * Verify that this message was targeting this NSA by looking at the
     * ProviderNSA field. If invalid we will throw an exception.
     */

    /*
     * Get the connectionId from the request as this is used as our primary
     * index.
     */

    // Route this message to the appropriate actor for processing.

  }

  public void releaseFailed(Holder<String> correlationId, GenericFailedType releaseFailed) throws ServiceException {

    // Validate we received the confirmed message.

    // Build an internal request for this releaseFailed request.

    /*
     * Break out the attributes we need for handling.
     */

    /*
     * Extract NSA fields. These are currently named incorrectly as the
     * specification states they should contain the NsNetwork name. Try not to
     * get confused when looking up information in topology using this field.
     */

    /*
     * Verify that this message was targeting this NSA by looking at the
     * ProviderNSA field. If invalid we will throw an exception.
     */

    /*
     * Get the connectionId from the request as this is used as our primary
     * index.
     */

    // Route this message to the appropriate actor for processing.

  }

  public void terminateConfirmed(Holder<String> correlationId, GenericConfirmedType terminateConfirmed)
      throws ServiceException {

    // Validate we received the confirmed message.

    // Build an internal request for this releaseConfirmed request.

    /*
     * Break out the attributes we need for handling.
     */

    /*
     * Extract NSA fields. These are currently named incorrectly as the
     * specification states they should contain the NsNetwork name. Try not to
     * get confused when looking up information in topology using this field.
     */

    /*
     * Verify that this message was targeting this NSA by looking at the
     * ProviderNSA field. If invalid we will throw an exception.
     */

    /*
     * Get the connectionId from the request as this is used as our primary
     * index.
     */

    // Route this message to the appropriate actor for processing.

  }

  public void terminateFailed(Holder<String> correlationId, GenericFailedType terminateFailed) throws ServiceException {

    // Validate we received the confirmed message.

    // Build an internal request for this terminateFailed request.

    /*
     * Break out the attributes we need for handling.
     */

    /*
     * Extract NSA fields. These are currently named incorrectly as the
     * specification states they should contain the NsNetwork name. Try not to
     * get confused when looking up information in topology using this field.
     */

    /*
     * Verify that this message was targeting this NSA by looking at the
     * ProviderNSA field. If invalid we will throw an exception.
     */

    /*
     * Get the connectionId from the request as this is used as our primary
     * index.
     */

    // Route this message to the appropriate actor for processing.

  }

  public GenericAcknowledgmentType query(QueryRequestType parameters) throws ServiceException {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public void queryConfirmed(Holder<String> correlationId, QueryConfirmedType queryConfirmed) throws ServiceException {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public void queryFailed(Holder<String> correlationId, QueryFailedType queryFailed) throws ServiceException {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public GenericAcknowledgmentType forcedEnd(ForcedEndRequestType parameters) throws ServiceException {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

}

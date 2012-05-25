/**
 * Copyright (c) 2011, SURFnet bv, The Netherlands
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the name of the SURFnet bv, The Netherlands nor the names of
 *     its contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL SURFnet bv, The Netherlands BE LIABLE FOR
 * AND DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
package nl.surfnet.bod.web.services;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;

import org.ogf.schemas.nsi._2011._10.connection._interface.ForcedEndRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
import org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveConfirmedType;
import org.springframework.stereotype.Service;

@Service("nsiConnectionServiceRequester")
@WebService(serviceName = "ConnectionServiceRequester",
    portName = "ConnectionServiceRequesterPort",
    endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort",
    targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/requester",
    wsdlLocation = "/WEB-INF/wsdl/nsi/ogf_nsi_connection_requester_v1_0.wsdl")
public final class NsiConnectionServiceRequester extends NsiConnectionService {

  /*
   * This holds the web service request context which includes all the original
   * HTTP information, including the JAAS authentication and authorisation
   * information.
   */
  @Resource
  private WebServiceContext webServiceContext;

  @PostConstruct
  @SuppressWarnings("unused")
  private void init() {
    getLog().debug("webServiceContext: {}", webServiceContext);
  }

  @PreDestroy
  @SuppressWarnings("unused")
  private void destroy() {
  }

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

  public void query(QueryRequestType parameters) throws ServiceException {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public void queryConfirmed(Holder<String> correlationId, QueryConfirmedType queryConfirmed) throws ServiceException {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public void queryFailed(Holder<String> correlationId, QueryFailedType queryFailed) throws ServiceException {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public void forcedEnd(ForcedEndRequestType parameters) throws ServiceException {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

}

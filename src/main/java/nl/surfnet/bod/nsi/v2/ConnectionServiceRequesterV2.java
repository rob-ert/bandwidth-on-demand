/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.nsi.v2;

import static com.google.common.collect.Lists.transform;
import static nl.surfnet.bod.nsi.v2.ConnectionsV2.toQueryRecursiveResultType;
import static nl.surfnet.bod.nsi.v2.ConnectionsV2.toQuerySummaryResultType;

import java.util.List;

import javax.annotation.Resource;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.util.XmlUtils;

import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStatusType;
import org.ogf.schemas.nsi._2013._04.connection.types.LifecycleStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryRecursiveResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._04.framework.types.TypeValuePairListType;
import org.springframework.stereotype.Component;

@Component
public class ConnectionServiceRequesterV2 {

  @Resource private ConnectionV2Repo connectionRepo;
  @Resource private ConnectionServiceRequesterClient client;

  public void reserveConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setReservationState(ReservationStateEnumType.RESERVE_HELD);
    connectionRepo.save(connection);

    ReservationConfirmCriteriaType criteria = new ReservationConfirmCriteriaType()
      .withBandwidth(connection.getDesiredBandwidth())
      .withPath(connection.getPath())
      .withSchedule(connection.getSchedule())
      .withServiceAttributes(new TypeValuePairListType())
      .withVersion(0);

    client.sendReserveConfirmed(
        requestDetails.getCommonHeaderType(),
        connection.getConnectionId(),
        connection.getGlobalReservationId(),
        connection.getDescription(),
        ImmutableList.of(criteria),
        requestDetails.getReplyTo());
  }

  public void abortConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setReservationState(ReservationStateEnumType.RESERVE_START);
    connectionRepo.save(connection);

    client.sendAbortConfirmed(requestDetails.getCommonHeaderType(), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void terminateConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setLifecycleState(LifecycleStateEnumType.TERMINATED);
    connectionRepo.save(connection);

    client.sendTerminateConfirmed(requestDetails.getCommonHeaderType(), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void reserveAbortConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setReservationState(ReservationStateEnumType.RESERVE_START);
    connectionRepo.save(connection);

    client.sendReserveAbortConfirmed(requestDetails.getCommonHeaderType(), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void reserveCommitConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setReservationState(ReservationStateEnumType.RESERVE_START);
    connection.setProvisionState(ProvisionStateEnumType.RELEASED);
    connection.setLifecycleState(LifecycleStateEnumType.CREATED);
    connectionRepo.save(connection);

    client.sendReserveCommitConfirmed(requestDetails.getCommonHeaderType(), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void provisionConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setProvisionState(ProvisionStateEnumType.PROVISIONED);
    connectionRepo.save(connection);

    client.sendProvisionConfirmed(requestDetails.getCommonHeaderType(), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void dataPlaneActivated(Long id, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setDataPlaneActive(true);
    connectionRepo.save(connection);

    sendDataPlaneStatus(requestDetails, connection, DateTime.now());
  }

  public void dataPlaneDeactivated(Long id, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setDataPlaneActive(false);
    connectionRepo.save(connection);

    sendDataPlaneStatus(requestDetails, connection, DateTime.now());
  }

  private void sendDataPlaneStatus(NsiRequestDetails requestDetails, ConnectionV2 connection, DateTime when) {
    DataPlaneStatusType dataPlaneStatus = new DataPlaneStatusType().withActive(connection.isDataPlaneActive()).withVersion(0).withVersionConsistent(true);
    XMLGregorianCalendar timeStamp = XmlUtils.toGregorianCalendar(when);

    CommonHeaderType header = requestDetails.getCommonHeaderType().withCorrelationId(NsiHelper.generateCorrelationId());

    client.sendDataPlaneStatus(header, connection.getConnectionId(), dataPlaneStatus, timeStamp, requestDetails.getReplyTo());
  }

  public void querySummaryConfirmed(List<ConnectionV2> connections, NsiRequestDetails requestDetails) {
    List<QuerySummaryResultType> results = transform(connections, toQuerySummaryResultType);

    client.sendQuerySummaryConfirmed(requestDetails.getCommonHeaderType(), results, requestDetails.getReplyTo());
  }

  public void queryRecursiveConfirmed(List<ConnectionV2> connections, NsiRequestDetails requestDetails){
    List<QueryRecursiveResultType> result = transform(connections, toQueryRecursiveResultType);

    client.sendQueryRecursiveConfirmed(requestDetails.getCommonHeaderType(), result, requestDetails.getReplyTo());
  }

}

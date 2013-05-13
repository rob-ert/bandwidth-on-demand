package nl.surfnet.bod.nsi.v2;

import java.util.Arrays;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.util.XmlUtils;

import org.ogf.schemas.nsi._2013._04.connection.types.*;
import org.ogf.schemas.nsi._2013._04.framework.types.TypeValuePairListType;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

public final class ConnectionsV2 {

  public static final Function<ConnectionV2, QuerySummaryResultType> toQuerySummaryResultType = new Function<ConnectionV2, QuerySummaryResultType>() {
    public QuerySummaryResultType apply(ConnectionV2 connection) {

      return new QuerySummaryResultType()
        .withRequesterNSA("requester")
        .withCriteria(new ReservationConfirmCriteriaType()
          .withBandwidth(connection.getDesiredBandwidth())
          .withSchedule(new ScheduleType()
            .withEndTime(XmlUtils.toGregorianCalendar(connection.getEndTime().get()))
            .withStartTime(XmlUtils.toGregorianCalendar(connection.getStartTime().get())))
          .withVersion(0)
          .withServiceAttributes(new TypeValuePairListType())
          .withPath(new PathType()
            .withSourceSTP(toStpType(connection.getSourceStpId()))
            .withDestSTP(toStpType(connection.getDestinationStpId()))
            .withDirectionality(DirectionalityType.BIDIRECTIONAL)))
        .withConnectionId(connection.getConnectionId())
        .withConnectionStates(new ConnectionStatesType()
          .withReservationState(new ReservationStateType().withState(connection.getReservationState()).withVersion(0))
          .withLifecycleState(new LifecycleStateType().withState(connection.getLifecycleState()).withVersion(0))
          .withProvisionState(new ProvisionStateType().withState(connection.getProvisionState()).withVersion(0))
          .withDataPlaneStatus(new DataPlaneStatusType().withActive(false).withVersionConsistent(true).withVersion(0)));
    }
  };

  private ConnectionsV2() {
  }

  public static  StpType toStpType(String sourceStpId) {
    String[] parts = sourceStpId.split(":");
    String networkId = Joiner.on(":").join(Arrays.copyOfRange(parts, 0, parts.length - 2));

    return new StpType().withNetworkId(networkId).withLocalId(parts[parts.length - 1]);
  }

}

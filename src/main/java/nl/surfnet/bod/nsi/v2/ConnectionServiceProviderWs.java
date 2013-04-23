package nl.surfnet.bod.nsi.v2;

import static com.google.common.base.Strings.emptyToNull;

import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebServiceContext;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.service.ConnectionService;
import nl.surfnet.bod.service.ConnectionService.ValidationException;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.XmlUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.ogf.schemas.nsi._2013._04.connection.provider.ConnectionProviderPort;
import org.ogf.schemas.nsi._2013._04.connection.provider.QuerySummarySyncFailed;
import org.ogf.schemas.nsi._2013._04.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryFailedType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryRecursiveResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationRequestCriteriaType;
import org.ogf.schemas.nsi._2013._04.connection.types.StpType;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._04.framework.types.ServiceExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.SchemaValidation;

@Service("connectionServiceProviderWs_v2")
@WebService(serviceName = "ConnectionServiceProvider",
  portName = "ConnectionServiceProviderPort",
  endpointInterface = "org.ogf.schemas.nsi._2013._04.connection.provider.ConnectionProviderPort",
  targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/provider")
@SchemaValidation
public class ConnectionServiceProviderWs implements ConnectionProviderPort {

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceProviderWs.class);

  @Resource private WebServiceContext context;
  @Resource private ConnectionService connectionService;
  @Resource private Environment bodEnvironment;

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/reserve")
  @RequestWrapper(localName = "reserve", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.ReserveType")
  @ResponseWrapper(localName = "reserveResponse", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.ReserveResponseType")
  public void reserve(
      @WebParam(name = "globalReservationId", targetNamespace = "") String globalReservationId,
      @WebParam(name = "description", targetNamespace = "") String description,
      @WebParam(name = "connectionId", targetNamespace = "", mode = Mode.INOUT) Holder<String> connectionId,
      @WebParam(name = "criteria", targetNamespace = "") ReservationRequestCriteriaType criteria)
      throws ServiceException {

    log.info("Received a NSI v2 reserve request");

    CommonHeaderType header = getNsiHeader();

    NsiRequestDetails requestDetails = new NsiRequestDetails(header.getReplyTo(), header.getReplyTo());

    Connection connection = createConnection(
        Optional.fromNullable(emptyToNull(globalReservationId)),
        Optional.fromNullable(emptyToNull(description)),
        header.getProviderNSA(),
        header.getRequesterNSA(),
        criteria);

    connectionId.value = connection.getConnectionId();

    reserve(connection, requestDetails, Security.getUserDetails());
  }

  private CommonHeaderType getNsiHeader() throws ServiceException {
    try {
      HeaderList headerList = (HeaderList) context.getMessageContext().get(JAXWSProperties.INBOUND_HEADER_LIST_PROPERTY);
      Header nsiHeader = headerList.get("http://schemas.ogf.org/nsi/2013/04/framework/headers", "nsiHeader", false);
      return nsiHeader.<JAXBElement<CommonHeaderType>>readAsJAXB(JAXBContext.newInstance(CommonHeaderType.class).createUnmarshaller()).getValue();
    } catch (JAXBException e) {
      ServiceExceptionType faultInfo = new ServiceExceptionType()
        .withErrorId(e.getErrorCode())
        .withNsaId(bodEnvironment.getNsiProviderNsa())
        .withText(e.getMessage());

      throw new ServiceException("Could not parse NSI header", faultInfo, e);
    }
  }

  protected void reserve(Connection connection, NsiRequestDetails request, RichUserDetails richUserDetails) throws ServiceException {
    try {
      connectionService.reserve(connection, request, false, richUserDetails);
    } catch (ValidationException e) {
      ServiceExceptionType faultInfo = new ServiceExceptionType()
        .withErrorId(e.getErrorCode())
        .withNsaId(bodEnvironment.getNsiProviderNsa())
        .withText(e.getMessage());

      throw new ServiceException(e.getMessage(), faultInfo, e);
    }
  }

  private Connection createConnection(Optional<String> globalReservationId, Optional<String> description, String providerNsa, String requesterNsa, ReservationRequestCriteriaType criteria) {
    Optional<DateTime> startTime = XmlUtils.getDateFrom(criteria.getSchedule().getStartTime());
    Optional<DateTime> endTime = XmlUtils.getDateFrom(criteria.getSchedule().getEndTime());
    Connection connection = new Connection();
    connection.setCurrentState(ConnectionStateType.INITIAL); // NSI 2 states...
    connection.setConnectionId(NsiHelper.generateConnectionId());
    connection.setGlobalReservationId(globalReservationId.or(NsiHelper.generateGlobalReservationId()));
    connection.setDescription(description.orNull());
    connection.setStartTime(startTime.orNull());
    connection.setEndTime(endTime.orNull());
    connection.setDesiredBandwidth(criteria.getBandwidth());
    connection.setProtectionType(ProtectionType.PROTECTED.name());
    connection.setSourceStpId(stpTypeToStpId(criteria.getPath().getSourceSTP()));
    connection.setDestinationStpId(stpTypeToStpId(criteria.getPath().getDestSTP()));
    connection.setProviderNsa(providerNsa);
    connection.setRequesterNsa(requesterNsa);

    // FIXME this is NSI 1.0 stuff
    connection.setPath(new PathType());
    connection.setServiceParameters(new ServiceParametersType());

    return connection;
  }

  private String stpTypeToStpId(StpType type) {
    return type.getNetworkId() + ":" + type.getLocalId();
  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/reserveCommit")
  @RequestWrapper(localName = "reserveCommit", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.GenericRequestType")
  @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/interface", className = "org.ogf.schemas.nsi._2013._04.connection._interface.GenericAcknowledgmentType")
  public void reserveCommit(@WebParam(name = "connectionId", targetNamespace = "") String connectionId)
      throws ServiceException {

    notImplementedYet();
  }

  private void notImplementedYet() throws ServiceException {
    ServiceExceptionType faultInfo = new ServiceExceptionType().withNsaId(bodEnvironment.getNsiProviderNsa())
        .withErrorId("0100").withText("This operation is not implemented yet");
    throw new ServiceException("", faultInfo);
  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/reserveAbort")
  @RequestWrapper(localName = "reserveAbort", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.GenericRequestType")
  @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/interface", className = "org.ogf.schemas.nsi._2013._04.connection._interface.GenericAcknowledgmentType")
  public void reserveAbort(@WebParam(name = "connectionId", targetNamespace = "") String connectionId)
      throws ServiceException {

    notImplementedYet();
  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/provision")
  @RequestWrapper(localName = "provision", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.GenericRequestType")
  @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/interface", className = "org.ogf.schemas.nsi._2013._04.connection._interface.GenericAcknowledgmentType")
  public void provision(@WebParam(name = "connectionId", targetNamespace = "") String connectionId)
      throws ServiceException {

    notImplementedYet();
  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/release")
  @RequestWrapper(localName = "release", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.GenericRequestType")
  @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/interface", className = "org.ogf.schemas.nsi._2013._04.connection._interface.GenericAcknowledgmentType")
  public void release(@WebParam(name = "connectionId", targetNamespace = "") String connectionId)
      throws ServiceException {

    notImplementedYet();
  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/terminate")
  @RequestWrapper(localName = "terminate", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.GenericRequestType")
  @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/interface", className = "org.ogf.schemas.nsi._2013._04.connection._interface.GenericAcknowledgmentType")
  public void terminate(@WebParam(name = "connectionId", targetNamespace = "") String connectionId)
      throws ServiceException {

    notImplementedYet();
  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/querySummary")
  @RequestWrapper(localName = "querySummary", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.QueryType")
  @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/interface", className = "org.ogf.schemas.nsi._2013._04.connection._interface.GenericAcknowledgmentType")
  public void querySummary(@WebParam(name = "connectionId", targetNamespace = "") List<String> connectionId,
      @WebParam(name = "globalReservationId", targetNamespace = "") List<String> globalReservationId)
      throws ServiceException {

    notImplementedYet();
  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/querySummaryConfirmed")
  @RequestWrapper(localName = "querySummaryConfirmed", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryConfirmedType")
  @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/interface", className = "org.ogf.schemas.nsi._2013._04.connection._interface.GenericAcknowledgmentType")
  public void querySummaryConfirmed(
      @WebParam(name = "reservation", targetNamespace = "") List<QuerySummaryResultType> reservation)
      throws ServiceException {
    // TODO Auto-generated method stub

    notImplementedYet();
  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/querySummaryFailed")
  @RequestWrapper(localName = "querySummaryFailed", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.QueryFailedType")
  @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/interface", className = "org.ogf.schemas.nsi._2013._04.connection._interface.GenericAcknowledgmentType")
  public void querySummaryFailed(
      @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException)
      throws ServiceException {
    // TODO Auto-generated method stub

    notImplementedYet();
  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/queryRecursive")
  @RequestWrapper(localName = "queryRecursive", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.QueryType")
  @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/interface", className = "org.ogf.schemas.nsi._2013._04.connection._interface.GenericAcknowledgmentType")
  public void queryRecursive(@WebParam(name = "connectionId", targetNamespace = "") List<String> connectionId,
      @WebParam(name = "globalReservationId", targetNamespace = "") List<String> globalReservationId)
      throws ServiceException {

    notImplementedYet();
  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/queryRecursiveConfirmed")
  @RequestWrapper(localName = "queryRecursiveConfirmed", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.QueryRecursiveConfirmedType")
  @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/interface", className = "org.ogf.schemas.nsi._2013._04.connection._interface.GenericAcknowledgmentType")
  public void queryRecursiveConfirmed(
      @WebParam(name = "reservation", targetNamespace = "") List<QueryRecursiveResultType> reservation)
      throws ServiceException {

    notImplementedYet();
  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/queryRecursiveFailed")
  @RequestWrapper(localName = "queryRecursiveFailed", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.QueryFailedType")
  @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/interface", className = "org.ogf.schemas.nsi._2013._04.connection._interface.GenericAcknowledgmentType")
  public void queryRecursiveFailed(
      @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException)
      throws ServiceException {

    notImplementedYet();
  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/querySummarySync")
  @WebResult(name = "reservation", targetNamespace = "")
  @RequestWrapper(localName = "querySummarySync", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.QueryType")
  @ResponseWrapper(localName = "querySummarySyncConfirmed", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryConfirmedType")
  public List<QuerySummaryResultType> querySummarySync(
      @WebParam(name = "connectionId", targetNamespace = "") List<String> connectionId,
      @WebParam(name = "globalReservationId", targetNamespace = "") List<String> globalReservationId)
      throws QuerySummarySyncFailed {

    QueryFailedType faultInfo = new QueryFailedType().withServiceException(new ServiceExceptionType()
        .withErrorId("0100")
        .withNsaId(bodEnvironment.getNsiProviderNsa())
        .withText("Not implemented yet"));

    throw new QuerySummarySyncFailed("Not implemented yet", faultInfo);
  }

}

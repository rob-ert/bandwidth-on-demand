package nl.surfnet.bod.nsi.v2;

import nl.surfnet.bod.domain.NsiRequestDetails;

import org.ogf.schemas.nsi._2013._04.connection.requester.ConnectionRequesterPort;
import org.springframework.stereotype.Component;

@Component("connectionServiceRequesterVersion2")
public class ConnectionServiceRequesterCallback {

//  private static final String WSDL_LOCATION = "/wsdl/ogf_nsi_connection_requester_v1_0.wsdl";

  public ConnectionRequesterPort apply(NsiRequestDetails requestDetails) {
    //ConnectionRequesterPort port = new ConnectionServiceRequester(wsdlLocation, serviceName)

        //new ConnectionServiceRequester(getWsdlUrl(), SERVICE_NAME).getConnectionServiceRequesterPort();

//    Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
//    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, requestDetails.getReplyTo());
    return null;
  }
}

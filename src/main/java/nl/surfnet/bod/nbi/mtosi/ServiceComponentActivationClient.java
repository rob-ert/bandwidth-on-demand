package nl.surfnet.bod.nbi.mtosi;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import nl.surfnet.bod.domain.Reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ReserveException;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ServiceComponentActivationInterface;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ServiceComponentActivationInterfaceHttp;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveResponse;

import com.google.common.base.Throwables;

@Service
public class ServiceComponentActivationClient {

  private static final String WSDL_LOCATION = "/mtosi/2.1/DDPs/ServiceActivation/IIS/wsdl/ServiceComponentActivationInterface/ServiceComponentActivationInterfaceHttp.wsdl";

  private final String endPoint;
  private final ServiceComponentActivationInterfaceHttp client;

  @Autowired
  public ServiceComponentActivationClient(@Value("${mtosi.service.reserve.endpoint}") String endPoint) {
      this.endPoint = endPoint;
      URL wsdlUrl = this.getClass().getResource(WSDL_LOCATION);
      this.client = new ServiceComponentActivationInterfaceHttp(
        wsdlUrl,
        new QName("http://www.tmforum.org/mtop/sa/wsdl/scai/v1-0", "ServiceComponentActivationInterfaceHttp"));
  }

  public void reserve(Reservation reservation, boolean autoProvision) {
    Holder<Header> header = HeaderBuilder.buildReserveHeader(endPoint);
    ReserveRequest reserveRequest = new ReserveRequestBuilder().createReservationRequest(reservation, autoProvision);

    try {
      ServiceComponentActivationInterface proxy = client.getServiceComponentActivationInterfaceSoapHttp();
      ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);

      ReserveResponse reserveResponse = proxy.reserve(header,
          reserveRequest);
      System.err.println("---->>");
      System.err.println(reserveResponse);
      System.err.println("<<----");
      System.err.println(reserveResponse.getRfsNameOrRfsCreation());
      // TODO do something with the reserveResponse..
    }
    catch (ReserveException e) {
      e.printStackTrace();
      Throwables.propagate(e);
    }
  }

}

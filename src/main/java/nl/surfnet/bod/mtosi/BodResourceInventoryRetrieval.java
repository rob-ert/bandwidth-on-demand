package nl.surfnet.bod.mtosi;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceClient;

import org.springframework.core.io.ClassPathResource;
import org.tmforum.mtop.mri.wsdl.rir.v1_0.ResourceInventoryRetrievalHttp;

@WebServiceClient(name = "ResourceInventoryRetrievalHttp",
    targetNamespace = "http://www.tmforum.org/mtop/mri/wsdl/rir/v1-0"
/*
 * , wsdlLocation =
 * "file:/Users/robert/work/offline/clients/surfnet/bandwidth-on-demand/src/main/resources/MTOSI-2.1/DDPs/ManageResourceInventory/IIS/wsdl/ResourceInventoryRetrieval/ResourceInventoryRetrievalHttp.wsdl"
 */)
public class BodResourceInventoryRetrieval extends ResourceInventoryRetrievalHttp {

  public BodResourceInventoryRetrieval() throws IOException {

    super(
        new ClassPathResource(
            "/MTOSI-2.1/DDPs/ManageResourceInventory/IIS/wsdl/ResourceInventoryRetrieval/ResourceInventoryRetrievalHttp.wsdl")
            .getURL(), new QName("http://www.tmforum.org/mtop/mri/wsdl/rir/v1-0", "ResourceInventoryRetrievalHttp"));
  }

}

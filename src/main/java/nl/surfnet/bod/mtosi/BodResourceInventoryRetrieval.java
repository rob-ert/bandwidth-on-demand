package nl.surfnet.bod.mtosi;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceClient;

import org.springframework.core.io.ClassPathResource;
import org.tmforum.mtop.mri.wsdl.rir.v1_0.ResourceInventoryRetrievalHttp;

@WebServiceClient(name = "ResourceInventoryRetrievalHttp",
    targetNamespace = "http://www.tmforum.org/mtop/mri/wsdl/rir/v1-0")
public class BodResourceInventoryRetrieval extends ResourceInventoryRetrievalHttp {

  public BodResourceInventoryRetrieval() throws IOException {

    super(
        new ClassPathResource(
            "/mtosi/2.1/DDPs/ManageResourceInventory/IIS/wsdl/ResourceInventoryRetrieval/ResourceInventoryRetrievalHttp.wsdl")
            .getURL(), new QName("http://www.tmforum.org/mtop/mri/wsdl/rir/v1-0", "ResourceInventoryRetrievalHttp"));
  }

}

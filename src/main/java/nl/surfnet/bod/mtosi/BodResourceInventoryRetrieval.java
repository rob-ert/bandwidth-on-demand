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

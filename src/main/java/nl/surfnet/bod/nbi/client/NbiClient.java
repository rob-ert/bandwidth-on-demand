package nl.surfnet.bod.nbi.client;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.surfnet.bod.nbi.client.generated.InventoryResponse;
import nl.surfnet.bod.nbi.client.generated.TerminationPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.adventnet.security.authentication.RMIAccessAPI;
import com.adventnet.security.authentication.RMIAccessException;
import com.esm.server.api.oss.OSSHandle;

@Service("nbiClient")
public class NbiClient {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	@Qualifier("nbiConfigurator")
	private NbiConfigurator nbbiConfigurator;

	private JAXBContext jaxbContext;
	private Unmarshaller unMarshaller;
	private OSSHandle ossHandle;

	@SuppressWarnings("unused")
	@PostConstruct
	private void init() throws RemoteException, RMIAccessException,
	    MalformedURLException, NotBoundException, JAXBException {
		log.info("Connecting with username {} to: {}",
		    nbbiConfigurator.getUsername(), nbbiConfigurator.getUrl());
		final RMIAccessAPI rmiAccessApi = (RMIAccessAPI) Naming
		    .lookup(nbbiConfigurator.getUrl());

		log.info("Looked up EMS RMI access API: {}", rmiAccessApi);
		ossHandle = (OSSHandle) rmiAccessApi.getAPI(nbbiConfigurator.getUsername(),
		    nbbiConfigurator.getPassword(), "OSSHandle");
		log.info("Looked up OSS handle: {}", ossHandle);

		jaxbContext = JAXBContext
		    .newInstance("nl.surfnet.bod.nbi.client.generated");
		unMarshaller = jaxbContext.createUnmarshaller();

	}

	public List<TerminationPoint> getAllPorts() {
		try {
			final String allPortsXml = ossHandle.getInventory(
			    nbbiConfigurator.getUsername(), nbbiConfigurator.getPassword(),
			    "getResourcesWithAttributes", "type=Port", null);
			log.debug("Retrieved all ports: {}", allPortsXml);
			return ((InventoryResponse) unMarshaller.unmarshal(new StringReader(
			    allPortsXml))).getTerminationPoint();
		}
		catch (Exception e) {
			log.error("Error: ", e);
			return null;
		}
	}

}

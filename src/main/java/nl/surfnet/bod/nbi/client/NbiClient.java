package nl.surfnet.bod.nbi.client;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.surfnet.bod.nbi.client.generated.InventoryResponse;
import nl.surfnet.bod.nbi.client.generated.TerminationPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adventnet.security.authentication.RMIAccessAPI;
import com.adventnet.security.authentication.RMIAccessException;
import com.esm.server.api.oss.OSSHandle;

public class NbiClient {

	private final Logger log = LoggerFactory.getLogger(getClass());

	// @Value("#{nbiProperties.nbiUsername}")
	private String username;

	// @Value("#{nbiProperties.nbiPassword}")
	private String password;

	// @Value("#{nbiProperties.nbiUrl}")
	private String url;

	private JAXBContext jaxbContext;
	private Unmarshaller unMarshaller;
	private OSSHandle ossHandle = null;

	private void init() throws RemoteException, RMIAccessException,
	    MalformedURLException, NotBoundException, JAXBException {
		log.info("Connecting with username {} to: {}", username, url);
		final RMIAccessAPI rmiAccessApi = (RMIAccessAPI) Naming.lookup(url);

		log.info("Looked up EMS RMI access API: {}", rmiAccessApi);
		ossHandle = (OSSHandle) rmiAccessApi
		    .getAPI(username, password, "OSSHandle");
		log.info("Looked up OSS handle: {}", ossHandle);

		jaxbContext = JAXBContext
		    .newInstance("nl.surfnet.bod.nbi.client.generated");
		unMarshaller = jaxbContext.createUnmarshaller();

	}

	public List<TerminationPoint> getAllPorts() {
		try {
			checkOssHandle();
			final String allPortsXml = ossHandle.getInventory(username, password,
			    "getResourcesWithAttributes", "type=Port", null);
			log.debug("Retrieved all ports: {}", allPortsXml);
			return ((InventoryResponse) unMarshaller.unmarshal(new StringReader(
			    allPortsXml))).getTerminationPoint();
		} catch (Exception e) {
			log.error("Error: ", e);
			return null;
		}
	}

	private void checkOssHandle() {
		if (ossHandle == null) {
			try {
				init();
			} catch (Exception e) {
				log.error("Error: ", e);
			}
		}
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

}

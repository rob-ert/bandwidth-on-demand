package nl.surfnet.bod.nbi.client;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import nl.surfnet.bod.nbi.client.generated.TerminationPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adventnet.security.authentication.RMIAccessException;

public class NbiClientMock implements NbiClient {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@SuppressWarnings("unused")
	@PostConstruct
	private void init() throws RemoteException, RMIAccessException,
	    MalformedURLException, NotBoundException, JAXBException {
		log.warn("U S I N G  M O C K  N B I  C L I E N T!");

	}

	@Override
	public List<TerminationPoint> getAllPorts() {
		return new ArrayList<TerminationPoint>();
	}

	public void setUsername(final String username) {
	}

	public void setPassword(final String password) {
	}

	public void setUrl(final String url) {
	}

}

package nl.surfnet.bod.nbi.client;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import nl.surfnet.bod.nbi.client.generated.GeneralBasic;
import nl.surfnet.bod.nbi.client.generated.PortDetail;
import nl.surfnet.bod.nbi.client.generated.TerminationPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adventnet.security.authentication.RMIAccessException;

public class NbiClientMock implements NbiClient {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @SuppressWarnings("unused")
    @PostConstruct
    private void init() throws RemoteException, RMIAccessException, MalformedURLException, NotBoundException,
            JAXBException {
        log.warn("U S I N G  M O C K  N B I  C L I E N T!");

    }

    @Override
    public List<TerminationPoint> getAllPorts() {
        ArrayList<TerminationPoint> terminationPoints = new ArrayList<TerminationPoint>();
        TerminationPoint tp = null;
        for (int index = 1; index < 10; index++) {
            tp = new TerminationPoint();
            tp.setPortBasic(new GeneralBasic());
            tp.setPortDetail(new PortDetail());

            tp.getPortBasic().setName("Name_" + index);
            tp.getPortBasic().setDisplayName("DisplayName_" + index);
            tp.getPortDetail().setPortId("PortId_" + index);

            terminationPoints.add(tp);
        }

        return terminationPoints;
    }

    public void setUsername(final String username) {
    }

    public void setPassword(final String password) {
    }

    public void setUrl(final String url) {
    }

}

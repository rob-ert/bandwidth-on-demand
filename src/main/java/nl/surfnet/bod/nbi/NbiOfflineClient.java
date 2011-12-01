package nl.surfnet.bod.nbi;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.surfnet.bod.nbi.generated.InventoryResponse;
import nl.surfnet.bod.nbi.generated.TerminationPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class NbiOfflineClient implements NbiClient {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private Unmarshaller unmarshaller;

  @PostConstruct
  protected void init() throws JAXBException {
    unmarshaller = JAXBContext.newInstance("nl.surfnet.bod.nbi.generated").createUnmarshaller();
    log.info("USING OFFLINE NBI CLIENT!");
  }

  @Override
  public TerminationPoint findPortsByName(String name) {
    return findAllPorts().get(1);
  }

  @Override
  public List<TerminationPoint> findAllPorts() {
    try {
      InputStream stream = getOfflineResponseFile();
      InventoryResponse inventoryResponse = (InventoryResponse) unmarshaller.unmarshal(stream);

      return Lists.transform(inventoryResponse.getTerminationPoint(), new Function<TerminationPoint, TerminationPoint>() {
        @Override
        public TerminationPoint apply(TerminationPoint input) {
          String oldName = input.getPortDetail().getName();
          input.getPortDetail().setName(oldName + "_dummy");
          return input;
        }
      });
    }
    catch (JAXBException e) {
      log.error("Could not load termination points from file", e);
      return Collections.emptyList();
    }
  }

  private InputStream getOfflineResponseFile() {
    return NbiOfflineClient.class.getResourceAsStream("/nbi_response.xml");
  }

}

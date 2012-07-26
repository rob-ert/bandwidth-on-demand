package nl.surfnet.bod.service;

import javax.xml.ws.Holder;

import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NsiConnectionService {

  private final Logger log = LoggerFactory.getLogger(NsiConnectionService.class);

  @Async
  public void sendQueryConfirmed(final String correlationId, QueryConfirmedType confirmedType,
      ConnectionRequesterPort port) {
    try {
      port.queryConfirmed(new Holder<>(correlationId), confirmedType);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      log.error("Error: ", e);
    }
  }

  @Async
  public void sendQueryFailed(final String correlationId, QueryFailedType failedType, ConnectionRequesterPort port) {
    try {
      port.queryFailed(new Holder<>(correlationId), failedType);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      log.error("Error: ", e);
    }
  }

}

package nl.surfnet.bod.nsi;

import java.util.concurrent.ConcurrentHashMap;

import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("simpelStateMachine")
class SimpelStateMachine implements StateMachine {
  private final Logger log = LoggerFactory.getLogger(getClass());

  public static final ConcurrentHashMap<String, ConnectionStateType> states = new ConcurrentHashMap<String, ConnectionStateType>();

  @Override
  public void inserOrUpdateState(String correlationId, ConnectionStateType state) {
    log.debug("Updating connection state of {} to {}", correlationId, state);
    states.put(correlationId, state);

  }

  @Override
  public void deleteState(String correlationId) {
    log.debug("Deleting connection state of {}", correlationId);

  }

  @Override
  public ConnectionStateType getState(String correlationId) {
    return states.get(correlationId);
  }

}

package nl.surfnet.bod.nsi;

import java.util.concurrent.ConcurrentHashMap;

import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("simpelStateMachine")
class SimpelStateMachine implements StateMachine {
  private static final ConcurrentHashMap<String, ConnectionStateType> STATES =
      new ConcurrentHashMap<String, ConnectionStateType>();

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  public void inserOrUpdateState(String correlationId, ConnectionStateType state) {
    log.debug("Updating connection state of {} to {}", correlationId, state);
    STATES.put(correlationId, state);

  }

  @Override
  public void deleteState(String correlationId) {
    log.debug("Deleting connection state of {}", correlationId);

  }

  @Override
  public ConnectionStateType getState(String correlationId) {
    return STATES.get(correlationId);
  }

}

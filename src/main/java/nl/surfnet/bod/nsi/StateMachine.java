package nl.surfnet.bod.nsi;

import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;

public interface StateMachine {
  
  
  void inserOrUpdateState(final String correlationId, ConnectionStateType state);
  
  void deleteState(final String correlationId);
  
  ConnectionStateType getState(final String correlationId);

}

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

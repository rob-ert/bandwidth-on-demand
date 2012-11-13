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
package nl.surfnet.bod.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventPublisher {

  private final Logger logger = LoggerFactory.getLogger(ReservationEventPublisher.class);

  private final List<ReservationListener> listeners = Collections.synchronizedList(new ArrayList<ReservationListener>());

  public void addListener(ReservationListener reservationListener) {
    listeners.add(reservationListener);
  }

  public void notifyListeners(ReservationStatusChangeEvent changeEvent) {
    logger.debug("Notifying {} listeners of event", listeners.size());

    synchronized (listeners) {
      for (ReservationListener listener : listeners) {
        logger.debug("Listener {}", listener.getClass());
        try {
          listener.onStatusChange(changeEvent);
        }
        catch (Throwable e) {
          logger.error("Failed to notify a listener " + listener, e);
          throw e;
        }
      }
    }
  }

}
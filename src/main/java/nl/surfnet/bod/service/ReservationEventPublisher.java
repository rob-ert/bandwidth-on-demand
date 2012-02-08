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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class ReservationEventPublisher {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final List<ReservationListener> listeners = Lists.newArrayList();

  public void addListener(ReservationListener reservationListener) {
    this.listeners.add(reservationListener);
  }

  public void notifyListeners(ReservationStatusChangeEvent changeEvent) {
    for (ReservationListener listener : listeners) {
      log.info("notify listeners {}", changeEvent);
      listener.onStatusChange(changeEvent);
    }
  }

}

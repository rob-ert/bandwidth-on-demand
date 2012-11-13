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
package nl.surfnet.bod.web.push;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import nl.surfnet.bod.service.ReservationEventPublisher;
import nl.surfnet.bod.service.ReservationListener;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class ReservationStatusChangeListener implements ReservationListener {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource
  private ReservationEventPublisher reservationEventPublisher;

  @Resource
  private EndPoints connections;

  @Resource
  private MessageSource messageSource;

  @PostConstruct
  public void registerListener() {
    reservationEventPublisher.addListener(this);
  }

  @Override
  public void onStatusChange(ReservationStatusChangeEvent reservationStatusChangeEvent) {
    PushMessage event = PushMessages.createMessage(reservationStatusChangeEvent, messageSource);
    logger.debug("Broadcasting event: {}", event);
    connections.broadcast(event);
  }
}
package nl.surfnet.bod.web;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

import org.joda.time.LocalDateTime;

public class ReservationView {
  private final Long id;
  private final String virtualResourceGroup;
  private final String sourcePort;
  private final String destinationPort;
  private final ReservationStatus status;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final Integer bandwidth;
  private final String userCreated;

  public ReservationView(Reservation reservation) {
    this.id = reservation.getId();
    this.virtualResourceGroup = reservation.getVirtualResourceGroup().getName();
    this.sourcePort = reservation.getSourcePort().getUserLabel();
    this.destinationPort = reservation.getDestinationPort().getUserLabel();
    this.status = reservation.getStatus();
    this.startDateTime = reservation.getStartDateTime();
    this.endDateTime = reservation.getEndDateTime();
    this.bandwidth = reservation.getBandwidth();
    this.userCreated = reservation.getUserCreated();
  }

  public String getVirtualResourceGroup() {
    return virtualResourceGroup;
  }

  public String getSourcePort() {
    return sourcePort;
  }

  public String getDestinationPort() {
    return destinationPort;
  }

  public ReservationStatus getStatus() {
    return status;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public Integer getBandwidth() {
    return bandwidth;
  }

  public Long getId() {
    return id;
  }

  public String getUserCreated() {
    return userCreated;
  }

}
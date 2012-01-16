package nl.surfnet.bod.service;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

import com.nortel.appcore.app.drac.common.types.TaskType.State;

public interface NbiService {

  /**
   * 
   * @param scheduleId
   */
  void cancelReservation(String scheduleId);

  /**
   * 
   * @return the amount of all available {@link PhysicalPort}'s
   */
  long getPhysicalPortsCount();

  /**
   * 
   * @param reservation
   * @return the reservation id returned from the underlying NMS
   */
  String createReservation(Reservation reservation);

  /**
   * Extends the schedule identified by the schedule id with an certain amount
   * of minutes
   * 
   * @param scheduleId
   * @param minutes
   */
  void extendReservation(String scheduleId, int minutes);

  /**
   * 
   * @return all available {@link PhysicalPort}'s
   */
  List<PhysicalPort> findAllPhysicalPorts();

  /**
   * 
   * @param name
   * @return a {@link PhysicalPort} identified by a certain name
   */
  PhysicalPort findPhysicalPortByName(String name);

  /**
   * 
   * @param scheduleId
   *          the id of the schedule of interest
   * @return {@link String} representation of an OpenDRAC schedule {@link State}
   *         . Possible values are {@link State#ABORTED}, {@link State#DONE},
   *         {@link State#IN_PROGRESS}, {@link State#SUBMITTED} (minus the
   *         "State." prefix) or <code>null</code> in case of an error.
   */
  ReservationStatus getReservationStatus(String scheduleId);

}
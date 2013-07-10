package nl.surfnet.bod.nbi.onecontrol;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.persistence.NoResultException;

import com.google.common.base.Optional;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.service.ReservationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReservationsAlignerTest {

  @InjectMocks
  private ReservationsAligner subject;

  @Mock
  private ReservationService reservationService;

  @Mock
  private NbiOneControlClient nbiOneControlClient;

  @Test
  public void poison_pill_returns_false(){
    subject.add(ReservationsAligner.POISON_PILL);
    assertFalse(subject.doAlign());
  }

  @Test
  public void reservation_status_not_updated_when_onecontrol_does_not_know_about_it() {
    final String unknownId = "unknown";
    subject.add(unknownId);
    when(nbiOneControlClient.getReservationStatus(unknownId)).thenReturn(Optional.<ReservationStatus>absent());

    assertTrue(subject.doAlign());

    verify(nbiOneControlClient, times(1)).getReservationStatus(unknownId);
    verifyNoMoreInteractions(reservationService);
  }

  @Test
  public void we_catch_NoResultException(){
    final String knownId = "known";
    ReservationStatus reserved = ReservationStatus.RESERVED;

    subject.add(knownId);
    assertTrue(subject.doAlign());

    when(nbiOneControlClient.getReservationStatus(knownId)).thenReturn(Optional.of(reserved));
    when(reservationService.updateStatus(knownId, reserved)).thenThrow(new NoResultException());
  }

  @Test
  public void happy_path(){
    final String knownId = "known";
    ReservationStatus reserved = ReservationStatus.RESERVED;
    subject.add(knownId);

    when(nbiOneControlClient.getReservationStatus(knownId)).thenReturn(Optional.of(reserved));
    when(reservationService.updateStatus(knownId, reserved)).thenReturn(new Reservation());

    assertTrue(subject.doAlign());
    verify(reservationService, times(1)).updateStatus(knownId, reserved);
  }

}

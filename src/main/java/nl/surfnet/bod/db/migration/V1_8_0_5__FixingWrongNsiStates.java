package nl.surfnet.bod.db.migration;

import nl.surfnet.bod.domain.ReservationStatus;

import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.springframework.jdbc.core.JdbcTemplate;

import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;

public class V1_8_0_5__FixingWrongNsiStates implements SpringJdbcMigration {

  @Override
  public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
    jdbcTemplate.update(queryChangeState(ConnectionStateType.CLEANING, ConnectionStateType.TERMINATED));
    jdbcTemplate.update(queryChangeState(ConnectionStateType.TERMINATING, ConnectionStateType.TERMINATED));

    jdbcTemplate.update(queryChangeState(ReservationStatus.TIMED_OUT, ConnectionStateType.TERMINATED));
    jdbcTemplate.update(queryChangeState(ReservationStatus.CANCELLED, ConnectionStateType.TERMINATED));
    jdbcTemplate.update(queryChangeState(ReservationStatus.SUCCEEDED, ConnectionStateType.TERMINATED));
  }

  private String queryChangeState(ConnectionStateType fromState, ConnectionStateType toState) {
    return String.format(
        "update connection set current_state = '%2$s' where reservation is null and current_state = '%1$s'",
        fromState, toState);
  }

  private String queryChangeState(ReservationStatus rStatus, ConnectionStateType cStatus) {
    return String.format(
        "update connection c set current_state = '%1$s' from reservation r where c.reservation = r.id and r.status = '%2$s' and c.current_state != '%1$s'", cStatus, rStatus);
  }

}

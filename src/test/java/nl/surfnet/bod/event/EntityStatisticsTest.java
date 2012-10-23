package nl.surfnet.bod.event;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.web.WebUtils;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EntityStatisticsTest {

  private final DateTime end = DateTime.now();
  private final DateTime start = end.minus(WebUtils.DEFAULT_REPORTING_PERIOD);

  @Test
  public void testEntityStatistics() {
    EntityStatistics<PhysicalPort> ppStats = new EntityStatistics<PhysicalPort>(PhysicalPort.class, start, 1L, 2L, 3L,
        end);

    assertThat(ppStats.getDomainObjectClass(), is(LogEvent.getDomainObjectName(PhysicalPort.class)));
    assertThat(ppStats.getDomainObjectKey(), is("label_" + ppStats.getDomainObjectClass().toLowerCase()));
    assertThat(ppStats.getPeriodStart(), is(start));
    assertThat(ppStats.getPeriodEnd(), is(end));

    assertThat(ppStats.getAmountCreated(), is((1L)));
    assertThat(ppStats.getAmountUpdated(), is((2L)));
    assertThat(ppStats.getAmountDeleted(), is((3L)));
  }
}

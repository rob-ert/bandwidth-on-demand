package nl.surfnet.bod.db.migration;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class V1_6_0_2__MigrateLogEventsTest {

  private final V1_6_0_2__MigrateLogEvents subject = new V1_6_0_2__MigrateLogEvents();

  @Test
  public void testSplitAtSemiColonWithMultipleColons() {
    String[] parts = subject.splitAtColon("Reservation: abc:123");
    assertThat(parts.length, is(2));
    assertThat(parts[0], is("Reservation"));
    assertThat(parts[1], is("abc:123"));
  }

  @Test
  public void testSplitAtSemiColonWithSingleColons() {
    String[] parts = subject.splitAtColon("Reservation: abc123");

    assertThat(parts.length, is(2));
    assertThat(parts[0], is("Reservation"));
    assertThat(parts[1], is("abc123"));
  }

  @Test
  public void testSplitAtSemiColonWithNoneColons() {
    String[] parts = subject.splitAtColon("Reservation abc123");

    assertThat(parts, nullValue());
  }

  @Test
  public void testSplitAtSemiColonWithEmptySecondPart() {
    String[] parts = subject.splitAtColon("Reservation:");

    assertThat(parts.length, is(2));
    assertThat(parts[0], is("Reservation"));
    assertThat(parts[1], is(""));
  }

  @Test
  public void testSplitAtSemiColonWithSpaceAsSecondPart() {
    String[] parts = subject.splitAtColon("Reservation: ");

    assertThat(parts.length, is(2));
    assertThat(parts[0], is("Reservation"));
    assertThat(parts[1], is(""));
  }

  @Test
  public void testSplitAtSemiColonEmpty() {
    String[] parts = subject.splitAtColon("");

    assertThat(parts, nullValue());
  }

  @Test
  public void testSplitAtSemiColonNull() {
    String[] parts = subject.splitAtColon(null);

    assertThat(parts, nullValue());
  }
}

package nl.surfnet.bod.util;

import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

public class TestHelper {

  public interface TimeTraveller<T> {
    public T apply() throws Exception;
  }

  public static <T> T runInPast(int timeAmount, TimeUnit unit, TimeTraveller<T> timeTraveller) {
    DateTime timeToRun = DateTime.now().minusMillis((int) TimeUnit.MILLISECONDS.convert(timeAmount, unit));
    return runAtSpecificTime(timeToRun, timeTraveller);
  }

  public static <T> T runAtSpecificTime(DateTime timeToRun, TimeTraveller<T> timeTraveller) {
    try {
      DateTimeUtils.setCurrentMillisFixed(timeToRun.getMillis());
      return timeTraveller.apply();
    }
    catch (Exception e) {
      throw new AssertionError(e);
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

}

package nl.surfnet.bod.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.jasypt.util.text.StrongTextEncryptor;
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

  public static String decryptProperty(String name, String env) {
    try {
      Properties props = new Properties();
      props.load(new FileInputStream(String.format("src/main/resources/env-properties/bod-%s.properties", env)));
      return decryptProperty(name, props);
    }
    catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  public static String decryptProperty(String name, Properties properties) {
    StrongTextEncryptor encryptor = new StrongTextEncryptor();
    encryptor.setPassword(System.getenv("BOD_ENCRYPTION_PASSWORD"));

    return PropertyValueEncryptionUtils.decrypt(properties.getProperty(name), encryptor);
  }

}

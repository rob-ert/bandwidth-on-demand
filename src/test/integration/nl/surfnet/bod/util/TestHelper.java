/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

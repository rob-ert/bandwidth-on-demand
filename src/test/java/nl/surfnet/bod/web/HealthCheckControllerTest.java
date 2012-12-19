package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.idd.IddClient;
import nl.surfnet.bod.support.KlantenFactory;
import nl.surfnet.bod.support.ModelStub;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.NOPLoggerFactory;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckControllerTest {

  @InjectMocks
  private HealthCheckController subject;

  @Mock
  private IddClient iddClientMock;

  @Test
  public void iddShouldPassTheHealthCheck() {
    supressErrorOutput();
    ModelStub model = new ModelStub();

    when(iddClientMock.getKlanten()).thenReturn(Lists.newArrayList(new KlantenFactory().create()));

    subject.index(model);

    assertThat((Boolean) model.asMap().get("iddHealth"), is(true));
    assertThat((Boolean) model.asMap().get("nbiHealth"), is(false));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void whenHealthCheckFailsShouldLog() {
    ModelStub model = new ModelStub();
    ErrorLogger logger = new ErrorLogger();
    subject.setLogger(logger);

    subject.index(model);

    assertThat(logger.getErrorMessages(), hasSize(4));
    assertThat(logger.getErrorMessages(), hasItems(containsString("IDD"), containsString("NBI"), containsString("OAuth"), containsString("API")));
  }

  private void supressErrorOutput() {
    subject.setLogger(new NOPLoggerFactory().getLogger(""));
  }

  @SuppressWarnings("serial")
  private class ErrorLogger extends MarkerIgnoringBase {

    private List<String> errorMessages = new ArrayList<>();

    public Collection<String> getErrorMessages() {
      return errorMessages;
    }

    @Override
    public synchronized void error(String format, Object... arguments) {
      errorMessages.add(MessageFormatter.format(format, arguments).getMessage());
    }

    @Override
    public synchronized void error(String format, Object arg) {
      errorMessages.add(MessageFormatter.format(format, arg).getMessage());
    }

    @Override
    public boolean isTraceEnabled() {
      return false;
    }

    @Override
    public void trace(String msg) {
    }

    @Override
    public void trace(String format, Object arg) {
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
    }

    @Override
    public void trace(String format, Object... arguments) {
    }

    @Override
    public void trace(String msg, Throwable t) {
    }

    @Override
    public boolean isDebugEnabled() {
      return false;
    }

    @Override
    public void debug(String msg) {
    }

    @Override
    public void debug(String format, Object arg) {
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
    }

    @Override
    public void debug(String format, Object... arguments) {
    }

    @Override
    public void debug(String msg, Throwable t) {
    }

    @Override
    public boolean isInfoEnabled() {
      return false;
    }

    @Override
    public void info(String msg) {
    }

    @Override
    public void info(String format, Object arg) {
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
    }

    @Override
    public void info(String format, Object... arguments) {
    }

    @Override
    public void info(String msg, Throwable t) {
    }

    @Override
    public boolean isWarnEnabled() {
      return false;
    }

    @Override
    public void warn(String msg) {
    }

    @Override
    public void warn(String format, Object arg) {
    }

    @Override
    public void warn(String format, Object... arguments) {
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
    }

    @Override
    public void warn(String msg, Throwable t) {
    }

    @Override
    public boolean isErrorEnabled() {
      return false;
    }

    @Override
    public void error(String msg) {
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
    }

    @Override
    public void error(String msg, Throwable t) {
    }

  }
}

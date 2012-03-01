package nl.surfnet.bod.web;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class MessageViewTest {
  private static final String INFO_VALUE = "info";

  private static final String WARN_VALUE = "warn";

  private static final String MESSAGE_KEY = "key";

  private static final String MESSAGE_VALUE = "some message";

  private MessageView message;

  @Mock
  private MessageSource messageSource;

  @Before
  public void setUp() {

    when(messageSource.getMessage("message_info", null, LocaleContextHolder.getLocale())).thenReturn(INFO_VALUE);
    when(messageSource.getMessage("message_warn", null, LocaleContextHolder.getLocale())).thenReturn(WARN_VALUE);
    when(messageSource.getMessage(MESSAGE_KEY, null, LocaleContextHolder.getLocale())).thenReturn(MESSAGE_VALUE);

  }

  @Test
  public void shouldSetInfoMessage() {
    message = MessageView.createInfoMessage(messageSource, MESSAGE_KEY);

    assertThat(message.getHeader(), is(INFO_VALUE));
    assertThat(message.getParagraph(), is(MESSAGE_VALUE));
  }

  @Test
  public void shouldSetWarnMessage() {
    message = MessageView.createWarningMessage(messageSource, MESSAGE_KEY);

    assertThat(message.getHeader(), is(WARN_VALUE));
    assertThat(message.getParagraph(), is(MESSAGE_VALUE));
  }

}

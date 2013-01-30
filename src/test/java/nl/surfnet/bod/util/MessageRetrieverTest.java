package nl.surfnet.bod.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class MessageRetrieverTest {
  private static final String TEST_MESSAGE = "test message";

  @InjectMocks
  private MessageRetriever subject;

  @Mock
  private MessageSource messageSourceMock;

  @Test
  public void shouldAddFlashMessage() {
    when(
        messageSourceMock.getMessage(eq("info_message"), aryEq(new String[] { "<b>een</b>", "<b>twee</b>" }),
            eq(LocaleContextHolder.getLocale()))).thenReturn(TEST_MESSAGE);

    String message = subject.getMessageWithBoldArguments("info_message", "een", "twee");

    assertThat(message, is(TEST_MESSAGE));
  }

  @Test
  public void shouldAddMessage() {
    when(
        messageSourceMock.getMessage(eq("info_message"), aryEq(new String[] { "<b>een</b>", "<b>twee</b>" }),
            eq(LocaleContextHolder.getLocale()))).thenReturn(TEST_MESSAGE);

    String message = subject.getMessageWithBoldArguments("info_message", "een", "twee");

    assertThat(message, is(TEST_MESSAGE));
  }
}

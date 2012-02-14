package nl.surfnet.bod.web;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import nl.surfnet.bod.support.ModelStub;

import org.junit.Test;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class WebUtilsTest {

  private Model model = new ModelStub();
  private RedirectAttributes redirectModel = new ModelStub();

  private final String messageBase = "Test message ";
  private final String message = messageBase + "{}";
  private final String htmlMessage = "<p>" + messageBase + "</p>";
  private final String messageArg = "unittest";
  private final Object[] messageArgs = { messageArg };
  private final Object[] emptyArgs = new Object[0];

  @Test
  public void testSecondPage() {
    assertThat(WebUtils.calculateFirstPage(2), is(WebUtils.MAX_ITEMS_PER_PAGE));
  }

  @Test
  public void shouldContainOnePage() {
    assertThat(WebUtils.calculateMaxPages(10), is(10 / WebUtils.MAX_ITEMS_PER_PAGE));
  }

  @Test
  public void shouldContainTenPages() {
    assertThat(WebUtils.calculateMaxPages(100), is(100 / WebUtils.MAX_ITEMS_PER_PAGE));
  }

  @Test
  public void shouldHaveReplacedPlaceholderWithNormalModel() {
    WebUtils.addInfoMessage(model, message, messageArgs);

    assertThat(WebUtils.getFirstInfoMessage(model), is(messageBase + messageArg));
  }

  @Test
  public void shouldHaveAddedTwoMessagesWithNormalModel() {
    WebUtils.addInfoMessage(model, message, messageArgs);
    WebUtils.addInfoMessage(model, "SecondMessage", emptyArgs);

    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) model.asMap().get(WebUtils.INFO_MESSAGES_KEY);
    assertThat(messages.get(0), is(messageBase + messageArg));
    assertThat(messages.get(1), is("SecondMessage"));
  }

  @Test
  public void shouldHaveReplacedPlaceholderWithNormalRedirectModel() {
    WebUtils.addInfoMessage(redirectModel, message, messageArgs);

    assertThat(WebUtils.getFirstInfoMessage(redirectModel), is(messageBase + messageArg));
  }

  @Test
  public void shouldHaveAddedTwoMessagesWithRedirectModel() {
    WebUtils.addInfoMessage(redirectModel, message, messageArgs);
    WebUtils.addInfoMessage(redirectModel, "SecondMessage", emptyArgs);

    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) redirectModel.getFlashAttributes().get(WebUtils.INFO_MESSAGES_KEY);
    assertThat(messages.get(0), is(messageBase + messageArg));
    assertThat(messages.get(1), is("SecondMessage"));
  }

  @Test
  public void shouldAddNullMessages() {
    WebUtils.addInfoMessage(model, null, emptyArgs);
    WebUtils.addInfoMessage(redirectModel, null, emptyArgs);
    WebUtils.addInfoMessageWithHtml(model, null, null, emptyArgs);
    WebUtils.addInfoMessageWithHtml(redirectModel, null, null, emptyArgs);
  }

  @Test
  public void shouldHaveAddHtmlMessageWithNormalModel() {
    WebUtils.addInfoMessageWithHtml(model, htmlMessage, "", emptyArgs);

    assertThat(WebUtils.getFirstInfoMessage(model), is(" " + htmlMessage));
  }

  @Test
  public void shouldHaveAddHtmlMessageWithRedirectModel() {
    WebUtils.addInfoMessageWithHtml(redirectModel, htmlMessage, "", emptyArgs);

    assertThat(WebUtils.getFirstInfoMessage(redirectModel), is(" " + htmlMessage));
  }

  @Test
  public void shouldFormatMessage() {
    String formatAndEscapeMessage = WebUtils.formatAndEscapeMessage(message, messageArgs);

    assertThat(formatAndEscapeMessage, is(messageBase + messageArg));
  }

  @Test
  public void shouldEscapeMessage() {
    String formatAndEscapeMessage = WebUtils.formatAndEscapeMessage("<p>", emptyArgs);

    assertThat(formatAndEscapeMessage, is("&lt;p&gt;"));
  }

  @Test
  public void shouldFormatAndEscapeMessage() {
    String formatAndEscapeMessage = WebUtils.formatAndEscapeMessage("<p>%s</p>", messageArgs);

    assertThat(formatAndEscapeMessage, is("&lt;p&gt;" + messageArg + "&lt;/p&gt;"));
  }

  @Test
  public void shouldAddMessagToModel() {
    WebUtils.addMessage(model, message);

    assertThat(WebUtils.getFirstInfoMessage(model), is(message));
    assertThat(WebUtils.getFirstInfoMessage(redirectModel), nullValue());
  }

  @Test
  public void shouldAddMessagToRedirectModel() {
    WebUtils.addMessage(redirectModel, message);

    assertThat(WebUtils.getFirstInfoMessage(redirectModel), is(message));
    assertThat(WebUtils.getFirstInfoMessage(model), nullValue());
  }

}

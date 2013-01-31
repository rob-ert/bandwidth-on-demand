package nl.surfnet.bod.web.base;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import nl.surfnet.bod.support.ModelStub;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Iterables;

@RunWith(MockitoJUnitRunner.class)
public class MessageManagerTest {
  private static final String TEST_MESSAGE_KEY = "test_key";
  private static final String TEST_MESSAGE = "test message";
  private static final String TEST_ARG = "argOne";
  private static final String TEST_HTML = "<button>Do not click me</button>";
  private static final String TEST_MESSAGE_WITH_HTML = TEST_MESSAGE + " " + TEST_HTML;

  @InjectMocks
  private MessageManager messageManager;

  @Mock
  private MessageRetriever messageRetriever;

  @Before
  public void setUp() {
    when(messageRetriever.getMessageWithBoldArguments(eq(TEST_MESSAGE_KEY), eq(TEST_ARG))).thenReturn(TEST_MESSAGE);
  }

  @Test
  public void shouldAddInfoFlashMessage() {
    RedirectAttributes redirectModel = new ModelStub();

    messageManager.addInfoFlashMessage(redirectModel, TEST_MESSAGE_KEY, TEST_ARG);

    verifyModel(redirectModel.getFlashAttributes(), MessageManager.INFO_MESSAGES_KEY, TEST_MESSAGE);
  }

  @Test
  public void shouldAddInfoFlashMessageWithAdditionalHtml() {
    RedirectAttributes redirectModel = new ModelStub();
    messageManager.addInfoFlashMessage(TEST_HTML, redirectModel, TEST_MESSAGE_KEY, TEST_ARG);

    verifyModel(redirectModel.getFlashAttributes(), MessageManager.INFO_MESSAGES_KEY, TEST_MESSAGE_WITH_HTML);
  }

  @Test
  public void shouldInfoAddMessage() {
    Model model = new ModelStub();

    messageManager.addInfoMessage(model, TEST_MESSAGE_KEY, TEST_ARG);

    verifyModel(model.asMap(), MessageManager.INFO_MESSAGES_KEY, TEST_MESSAGE);
  }

  @Test
  public void shouldAddErrorFlashMessage() {
    RedirectAttributes redirectModel = new ModelStub();

    messageManager.addErrorFlashMessage(redirectModel, TEST_MESSAGE_KEY, TEST_ARG);

    verifyModel(redirectModel.getFlashAttributes(), MessageManager.ERROR_MESSAGES_KEY, TEST_MESSAGE);
  }

  @Test
  public void shouldAddErrorMessage() {
    Model model = new ModelStub();

    messageManager.addErrorMessage(TEST_HTML, model, TEST_MESSAGE_KEY, TEST_ARG);

    verifyModel(model.asMap(), MessageManager.ERROR_MESSAGES_KEY, TEST_MESSAGE_WITH_HTML);
  }

  @Test
  public void shouldGetFirstInfoMessage() {
    Model model = new ModelStub();

    messageManager.addInfoMessage(model, TEST_MESSAGE_KEY, TEST_ARG);

    assertThat(messageManager.getFirstInfoMessage(model), is(TEST_MESSAGE));
  }

  @Test
  public void shouldGetFirstFlashInfoMessage() {
    RedirectAttributes redirectModel = new ModelStub();

    messageManager.addInfoFlashMessage(redirectModel, TEST_MESSAGE_KEY, TEST_ARG);

    assertThat(messageManager.getFirstInfoMessage(redirectModel), is(TEST_MESSAGE));
  }

  private void verifyModel(Map<String, ?> modelMap, String key, String expectedMessage) {
    @SuppressWarnings("unchecked")
    List<String> infoMessages = (List<String>) modelMap.get(key);

    assertThat(infoMessages, hasSize(1));
    assertThat(Iterables.getOnlyElement(infoMessages), is(expectedMessage));
  }

}

/**
 * Copyright (c) 2012, 2013 SURFnet BV
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

  private void verifyModel(Map<String, ?> modelMap, String key, String expectedMessage) {
    @SuppressWarnings("unchecked")
    List<String> infoMessages = (List<String>) modelMap.get(key);

    assertThat(infoMessages, hasSize(1));
    assertThat(Iterables.getOnlyElement(infoMessages), is(expectedMessage));
  }

}

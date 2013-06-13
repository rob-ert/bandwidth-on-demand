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
package nl.surfnet.bod.web.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.util.FullTextSearchResult;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.user.VirtualPortController.UpdateUserLabelCommand;
import nl.surfnet.bod.web.view.VirtualPortView;

import org.apache.lucene.queryParser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BeanPropertyBindingResult;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortControllerTest {

  @InjectMocks
  private VirtualPortController subject;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  private RichUserDetails user;

  @Before
  public void login() {
    user = new RichUserDetailsFactory().addUserGroup("urn:correct-group").create();
    Security.setUserDetails(user);
  }

  @Test
  public void list() {
    when(virtualPortServiceMock.findEntriesForUser(eq(user), eq(0), anyInt(), any(Sort.class))).thenReturn(
        Lists.newArrayList(new VirtualPortFactory().create()));

    ModelStub model = new ModelStub();
    subject.list(null, null, null, model);

    assertThat(model.asMap(), hasKey("list"));

    @SuppressWarnings("unchecked")
    Collection<VirtualPort> ports = (Collection<VirtualPort>) model.asMap().get("list");
    assertThat(ports, hasSize(1));
  }

  @Test
  public void updateFormForNonExistingPort() {
    when(virtualPortServiceMock.find(201L)).thenReturn(null);

    String page = subject.updateForm(201L, new ModelStub());

    assertThat(page, is("redirect:"));
  }

  @Test
  public void updateFormForIllegalPort() {
    VirtualResourceGroup group = new VirtualResourceGroupFactory().setAdminGroup("urn:wrong-group").create();
    VirtualPort port = new VirtualPortFactory().setVirtualResourceGroup(group).create();

    when(virtualPortServiceMock.find(201L)).thenReturn(port);

    ModelStub model = new ModelStub();
    String page = subject.updateForm(201L, model);

    assertThat(page, is("redirect:"));
  }

  @Test
  public void updateFormForPort() {
    VirtualResourceGroup group = new VirtualResourceGroupFactory().setAdminGroup("urn:correct-group").create();
    VirtualPort port = new VirtualPortFactory().setVirtualResourceGroup(group).create();

    when(virtualPortServiceMock.find(201L)).thenReturn(port);

    ModelStub model = new ModelStub();
    String page = subject.updateForm(201L, model);

    assertThat(model.asMap(), hasKey("virtualPort"));
    assertThat(page, is("virtualports/update"));
  }

  @Test
  public void updateForNonExistingPort() {
    UpdateUserLabelCommand command = new UpdateUserLabelCommand();
    command.setId(201L);

    when(virtualPortServiceMock.find(201L)).thenReturn(null);

    String page = subject.update(command, new BeanPropertyBindingResult(command, "command"), new ModelStub());

    assertThat(page, is("redirect:/virtualports"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldTranslateSearch() throws ParseException {
    ModelStub model = new ModelStub();

    VirtualPort port = new VirtualPortFactory().create();
    List<VirtualPort> result = Lists.newArrayList(port);

    when(virtualPortServiceMock.findEntriesForUser(eq(user), eq(0), eq(Integer.MAX_VALUE), any(Sort.class)))
        .thenReturn(result);

    when(virtualPortServiceMock.findIdsForUserUsingFilter(eq(user), any(VirtualPortView.class), any(Sort.class)))
        .thenReturn(new ArrayList<Long>());

    when(
        virtualPortServiceMock.searchForInFilteredList(eq(VirtualPort.class),
            eq("virtualResourceGroup.name:\"some-team\""), eq(0), eq(WebUtils.MAX_ITEMS_PER_PAGE), eq(user), anyList()))
        .thenReturn(new FullTextSearchResult<>(1, result));

    String page = subject.search(0, "userLabel", null, "team:\"some-team\"", model);

    assertThat(page, is("virtualports/list"));
    assertThat(model.asMap(), hasEntry("search", (Object) "team:\"some-team\""));
  }
}

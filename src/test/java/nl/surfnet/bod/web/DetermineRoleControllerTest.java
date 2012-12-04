/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.collect.Lists;

import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

public class DetermineRoleControllerTest {

  private DetermineRoleController subject = new DetermineRoleController();

  @Test
  public void aNocEngineerShouldBeRedirectToNocPage() {
    RichUserDetails user = new RichUserDetailsFactory().addNocRole().create();
    Security.setUserDetails(user);
    ModelStub model = new ModelStub();

    String page = subject.index(model, model);

    assertThat(page, is("redirect:noc"));
  }

  @Test
  public void aIctManagerShouldBeRedirectToManagerPage() {
    RichUserDetails user = new RichUserDetailsFactory().addManagerRole().create();
    Security.setUserDetails(user);
    ModelStub model = new ModelStub();

    String page = subject.index(model, model);

    assertThat(page, is("redirect:manager"));
  }

  @Test
  public void aUserShouldGoToIndex() {
    RichUserDetails user = new RichUserDetailsFactory().addUserRole().create();
    Security.setUserDetails(user);
    ModelStub model = new ModelStub();

    String page = subject.index(model, model);

    assertThat(page, is("redirect:user"));
  }

  @Test
  public void infoMessagesShouldBePreserved() {
    RichUserDetails user = new RichUserDetailsFactory().addUserRole().create();
    Security.setUserDetails(user);

    ModelStub model = new ModelStub();
    List<String> messages = Lists.newArrayList("First Message", "Second Messages");
    model.addAttribute(WebUtils.INFO_MESSAGES_KEY, messages);

    subject.index(model, model);

    assertThat(model.getFlashAttributes(), Matchers.<String, Object>hasEntry(WebUtils.INFO_MESSAGES_KEY, messages));
  }

}

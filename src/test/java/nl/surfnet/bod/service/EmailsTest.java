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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import nl.surfnet.bod.domain.VirtualPortCreateRequestLink;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortCreateRequestLinkFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.junit.Test;

public class EmailsTest {

  @Test
  public void requestVirtualPortMailWithEmail() {
    RichUserDetails from = new RichUserDetailsFactory().setDisplayname("Henk").setEmail("henk@henk.nl").create();
    VirtualPortCreateRequestLink requestLink = new VirtualPortCreateRequestLinkFactory().create();
    String link = "http://localhost";

    String body = Emails.VirtualPortCreateRequestMail.body(from, requestLink, link);

    assertThat(body, containsString("From: Henk (henk@henk.nl)"));
  }

  @Test
  public void requestVirtualPortMailWithoutEmail() {
    RichUserDetails from = new RichUserDetailsFactory().setDisplayname("Henk").setEmail(null).create();
    VirtualPortCreateRequestLink requestLink = new VirtualPortCreateRequestLinkFactory().create();
    String link = "http://localhost";

    String body = Emails.VirtualPortCreateRequestMail.body(from, requestLink, link);

    assertThat(body, containsString("From: Henk (Unknown Email)"));
  }
}

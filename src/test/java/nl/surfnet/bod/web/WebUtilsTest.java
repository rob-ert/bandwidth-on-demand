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
package nl.surfnet.bod.web;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WebUtilsTest {

  @Test
  public void testSecondPage() {
    assertThat(WebUtils.calculateFirstPage(2), is(WebUtils.MAX_ITEMS_PER_PAGE));
  }

  @Test
  public void shouldContainOnePage() {
    assertThat(WebUtils.calculateMaxPages(0), is(1));
    assertThat(WebUtils.calculateMaxPages(WebUtils.MAX_ITEMS_PER_PAGE), is(1));
    assertThat(WebUtils.calculateMaxPages(WebUtils.MAX_ITEMS_PER_PAGE - 1), is(1));
  }

  @Test
  public void shouldContainTwoPages() {
    assertThat(WebUtils.calculateMaxPages(WebUtils.MAX_ITEMS_PER_PAGE + 1), is(2));
    assertThat(WebUtils.calculateMaxPages(WebUtils.MAX_ITEMS_PER_PAGE + WebUtils.MAX_ITEMS_PER_PAGE), is(2));
  }

  @Test
  public void shouldShortenAdminGroup() {
    assertThat(WebUtils.shortenAdminGroup("a:b:c:d:e"), is("e"));
    assertThat(WebUtils.shortenAdminGroup("b:c:d:e"), is("e"));
    assertThat(WebUtils.shortenAdminGroup("c:d:e"), is("e"));
    assertThat(WebUtils.shortenAdminGroup("d:e"), is("e"));
    assertThat(WebUtils.shortenAdminGroup("e"), is("e"));

    assertThat(WebUtils.shortenAdminGroup(":"), is(""));
    assertThat(WebUtils.shortenAdminGroup(null), nullValue());
    assertThat(WebUtils.shortenAdminGroup(""), is(""));
  }

  @Test
  public void shouldMapTeam() {
    assertThat(WebUtils.replaceSearchWith("bladieblateam:hierendaar", "team", "virtualResourceGroup.name"),
        is("bladieblavirtualResourceGroup.name:hierendaar"));
  }

  @Test
  public void shouldMapNull() {
    assertThat(WebUtils.replaceSearchWith("bladieblateamhierendaar", null, null), is("bladieblateamhierendaar"));
  }
}

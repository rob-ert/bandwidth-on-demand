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
package nl.surfnet.bod.nbi.mtosi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nl.surfnet.bod.nbi.NbiMtosiClient;

import org.junit.Test;

public class InventoryRetrievalClientTest {

  private final InventoryRetrievalClient subject = new InventoryRetrievalClient("");

  @Test
  public void shouldRequireVlanForEVPL() {
    assertThat(subject.determineVlanRequired("EVPL"), is(true));
  }

  @Test
  public void shouldRequireVlanForEVPLAN() {
    assertThat(subject.determineVlanRequired("EVPLAN"), is(true));
  }

  @Test
  public void shouldNotRequireVlan() {
    assertThat(subject.determineVlanRequired("Bla"), is(false));
  }

  @Test
  public void shouldNotRequireVlanWhenNull() {
    assertThat(subject.determineVlanRequired(null), is(false));
  }

  @Test
  public void shouldNotRequireVlanWhenEmpty() {
    assertThat(subject.determineVlanRequired(""), is(false));
  }

  @Test
  public void shouldDisableRefreshOfCache() {
    InventoryRetrievalClient subject = new InventoryRetrievalClient("");
    subject.setNbiClientClass("someClass");
    assertFalse(subject.isMtosiEnabled());
  }

  @Test
  public void shouldEnableRefreshOfCache() {
    InventoryRetrievalClient subject = new InventoryRetrievalClient("");
    subject.setNbiClientClass(NbiMtosiClient.class.getName());
    assertTrue(subject.isMtosiEnabled());
  }

}

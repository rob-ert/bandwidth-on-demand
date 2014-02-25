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
package nl.surfnet.bod.nsi.v2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;

import nl.surfnet.bod.util.Environment;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceRequesterAsyncClientTest {

  @Mock
  private Environment bodEnvironment;

  @Mock
  private Optional<Map<String, String>> stunnelTranslationMap;

  @InjectMocks
  private ConnectionServiceRequesterAsyncClient subject;

  @Before
  public void before(){
    Map<String, String> entries = new HashMap<>();
    entries.put("foo.bar.com", "localhost:2000");
    entries.put("foo.bar.com:4443", "localhost:4000");

    when(bodEnvironment.isUseStunnelForNsiV2AsyncReplies()).thenReturn(true);
    when(stunnelTranslationMap.isPresent()).thenReturn(true);
    when(stunnelTranslationMap.get()).thenReturn(entries);

  }

  @Test
  public void basicMatch() throws Exception {
    // this should be a match, preserving fragment
    final Optional<URI> stunnelUri = subject.findStunnelUri(new URI("https://foo.bar.com/some/uri?it=true&stuff=1"));
    assertTrue(stunnelUri.get().toString().contains("localhost:2000"));
    assertTrue(stunnelUri.get().toString().endsWith("it=true&stuff=1"));
  }

  @Test
  public void matchWithSuperfluousPortNumber() throws Exception {
    // this should be a match, preserving fragment
    final Optional<URI> stunnelUri = subject.findStunnelUri(new URI("https://foo.bar.com:443/some/uri?it=true&stuff=1"));
    assertTrue(stunnelUri.get().toString().contains("localhost:2000"));
    assertTrue(stunnelUri.get().toString().endsWith("it=true&stuff=1"));
  }

  @Test
  public void matchWithNonStandardPortNumber() throws Exception {
    // this should be a match, preserving fragment
    final Optional<URI> stunnelUri = subject.findStunnelUri(new URI("https://foo.bar.com:4443/some/uri?it=true&stuff=1"));
    assertTrue(stunnelUri.get().toString().contains("localhost:4000"));
    assertTrue(stunnelUri.get().toString().endsWith("it=true&stuff=1"));
  }


  @Test
  public void basicNoMatch() throws Exception {
    final URI originalReplyUri = new URI("https://some.host.nl/foo.bar.com");
    final Optional<URI> stunnelUri = subject.findStunnelUri(originalReplyUri);
    assertFalse(stunnelUri.isPresent());
  }

  @Test
  public void httpNeverMatches() throws Exception {
    // this should be a match, preserving fragment
    final URI originalReplyUri = new URI("http://foo.bar.com/some/uri?it=true&stuff=1");
    final Optional<URI> stunnelUri = subject.findStunnelUri(originalReplyUri);
    assertFalse(stunnelUri.isPresent());
  }

  @Test
  public void noStunnelMapConfigured() throws Exception{
    when(stunnelTranslationMap.isPresent()).thenReturn(false);
    final URI originalReplyUri = new URI("https://foo.bar.com/some/uri?it=true&stuff=1");
    final Optional<URI> stunnelUri = subject.findStunnelUri(originalReplyUri);
    assertFalse(stunnelUri.isPresent());
  }

  @Test
  public void stunnelMapConfiguredButSslSwitchedOff() throws Exception{
    when(bodEnvironment.isUseStunnelForNsiV2AsyncReplies()).thenReturn(false);
    final URI originalReplyUri = new URI("https://foo.bar.com/some/uri?it=true&stuff=1");
    final Optional<URI> stunnelUri = subject.findStunnelUri(originalReplyUri);
    assertFalse(stunnelUri.isPresent());
  }

}
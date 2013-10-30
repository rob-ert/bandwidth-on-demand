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
package nl.surfnet.bod.web.noc;

import static nl.surfnet.bod.web.WebUtils.DATA_LIST;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Collection;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.support.NbiPortFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.noc.PhysicalPortController.PhysicalPortFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;


@RunWith(MockitoJUnitRunner.class)
public class UnallocatedPortControllerTest {

  @InjectMocks private UnallocatedPortController subject;

  @Mock private PhysicalPortService physicalPortServiceMock;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void listAllUnallocatedPortsShouldSetPorts() throws Exception {
    when(physicalPortServiceMock.findUnallocated()).thenReturn(
      FluentIterable.from(Iterables.cycle(new NbiPortFactory()
        .setSuggestedBodPortId("PortId")
        .setSuggestedNocLabel("NOC label").create())).limit(100).toList());
    when(physicalPortServiceMock.countUnallocated()).thenReturn(100L);

    mockMvc.perform(get("/noc/physicalports/free"))
      .andExpect(status().isOk())
      .andExpect(model().<Collection<?>> attribute(DATA_LIST, hasSize(WebUtils.MAX_ITEMS_PER_PAGE)))
      .andExpect(model().attribute("filterSelect", PhysicalPortFilter.UN_ALLOCATED));
  }

}

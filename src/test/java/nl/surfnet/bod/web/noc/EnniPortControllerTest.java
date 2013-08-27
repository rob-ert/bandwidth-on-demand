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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.NbiPort.InterfaceType;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.support.NbiPortFactory;
import nl.surfnet.bod.support.PhysicalPortFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(MockitoJUnitRunner.class)
public class EnniPortControllerTest {

  @InjectMocks
  private EnniPortController subject;

  @Mock private PhysicalPortService physicalPortServiceMock;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void bod_port_id_should_be_unique_create_enni() throws Exception {
    NbiPort nbiPort = new NbiPortFactory().setInterfaceType(InterfaceType.E_NNI).create();

    when(physicalPortServiceMock.findNbiPort("nmsPortId")).thenReturn(Optional.of(nbiPort));
    when(physicalPortServiceMock.findByBodPortId("duplicate")).thenReturn(new PhysicalPortFactory().create());

    mockMvc.perform(
      post("/noc/physicalports/enni")
        .param("nmsPortId", "nmsPortId")
        .param("bodPortId", "duplicate")
        .param("nocLabel", "Noc port name")
        .param("outboundPeer", "urn:ogf:network:outboud")
        .param("inboundPeer", "urn:ogf:network:inbound"))
      .andExpect(model().attributeErrorCount("createEnniPortCommand", 1))
      .andExpect(view().name("noc/physicalports/enni/create"));
  }

  @Test
  public void bod_port_id_should_be_unique_update_enni() throws Exception {
    EnniPort enni = (EnniPort) PhysicalPort.create(new NbiPortFactory().setInterfaceType(InterfaceType.E_NNI).create());
    enni.setBodPortId("oldid");

    when(physicalPortServiceMock.findByNmsPortId("nmsPortId")).thenReturn(enni);
    when(physicalPortServiceMock.findByBodPortId("duplicate")).thenReturn(new PhysicalPortFactory().create());

    mockMvc.perform(
      put("/noc/physicalports/enni")
        .param("nmsPortId", "nmsPortId")
        .param("bodPortId", "duplicate")
        .param("nocLabel", "Noc port name")
        .param("outboundPeer", "urn:ogf:network:outboud")
        .param("inboundPeer", "urn:ogf:network:inbound"))
      .andExpect(model().attributeErrorCount("updateEnniPortCommand", 1));
  }

}

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
package nl.surfnet.bod.web;

import java.util.Enumeration;

import com.google.common.net.HttpHeaders;
import java.util.Date;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import nl.surfnet.bod.service.TopologyService;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.ogf.schemas.nsi._2013._09.topology.NSAType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/nsi-topology")
public class NsiTopologyController {

  @Resource private TopologyService topologyService;

  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
  @ResponseBody
  public String topology(HttpServletRequest request, HttpServletResponse response) throws JAXBException, DateParseException {
    NSAType topology = topologyService.nsiTopology();
    Date lastModified = topology.getVersion().toGregorianCalendar().getTime();

    Enumeration<String> ifModifiedSinceValues = request.getHeaders(HttpHeaders.IF_MODIFIED_SINCE);
    if (ifModifiedSinceValues.hasMoreElements()) {
      Date ifModifiedSince = DateUtil.parseDate(ifModifiedSinceValues.nextElement());
      if (ifModifiedSince.equals(lastModified)) {
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return null;
      }
    }

    response.addHeader(HttpHeaders.LAST_MODIFIED, DateUtil.formatDate(lastModified));
    return TopologyService.NSA_TOPOLOGY_CONVERTER.toXmlString(topology);
  }

}

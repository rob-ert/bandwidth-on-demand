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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationConversionServiceFactoryBeanTest {

  @InjectMocks
  private ApplicationConversionServiceFactoryBean subject;

  @Mock private PhysicalPortService physicalPortServiceMock;

  @Test
  public void convetIdToUniPort() {
    UniPort port = new PhysicalPortFactory().create();

    when(physicalPortServiceMock.findUniPort(1L)).thenReturn(port);

    UniPort convertedPort = subject.getIdToUniPortConverter().convert(1L);

    assertThat(convertedPort, is(port));
  }

  @Test
  public void convertPhysicalResourceGroupToStringWithInstitute() {
    Institute institute = new InstituteFactory().setName("INST").create();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setInstitute(institute).create();

    String output = subject.getPhysicalResourceGroupToStringConverter().convert(group);

    assertThat(output, is("INST"));
  }

  @Test
  public void convertPhysicalResourceGroupToStringWithoutInstitute() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setInstitute(null).create();

    String output = subject.getPhysicalResourceGroupToStringConverter().convert(group);

    assertThat(output, nullValue());
  }

  @Test
  public void XmlGregorianCalendarToString() throws DatatypeConfigurationException {
    XMLGregorianCalendar input = DatatypeFactory.newInstance()
      .newXMLGregorianCalendar(2011, 2, 4, 12, 0, 10, 10, DatatypeConstants.FIELD_UNDEFINED);

    String output = subject.getXmlGregorianCalendarToStringConverter().convert(input);

    assertThat(output, is("2011-02-04 12:00"));
  }

}

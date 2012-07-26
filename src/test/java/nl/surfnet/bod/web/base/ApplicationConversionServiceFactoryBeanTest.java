/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.web.base;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationConversionServiceFactoryBeanTest {

  @InjectMocks
  private ApplicationConversionServiceFactoryBean subject;

  @Mock
  private PhysicalPortService physicalPortServiceMock;

  @Test
  public void convetIdToPhysicalPort() {
    PhysicalPort port = new PhysicalPortFactory().create();

    when(physicalPortServiceMock.find(1L)).thenReturn(port);

    PhysicalPort convertedPort = subject.getIdToPhysicalPortConverter().convert(1L);

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

}

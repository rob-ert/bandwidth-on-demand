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

import static nl.surfnet.bod.web.noc.EnniPortController.UpdateEnniPortCommand;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.validation.ConstraintViolation;

import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.support.PhysicalPortFactory;
import org.hibernate.validator.HibernateValidator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

public class UpdateEnniPortCommandTest {

  private LocalValidatorFactoryBean localValidatorFactory;
  private UpdateEnniPortCommand updateEnniPortCommand;
  private EnniPort enniPort;

  @Before
  public void setUp() throws Exception {
    localValidatorFactory = new LocalValidatorFactoryBean();
    localValidatorFactory.setProviderClass(HibernateValidator.class);
    localValidatorFactory.afterPropertiesSet();

    enniPort =  new PhysicalPortFactory().createEnni();
    updateEnniPortCommand = new UpdateEnniPortCommand(enniPort);
  }

  @Test
  public void should_mark_outboundPeer_invalid() {
    String validUrn = "urn:ogf:network:surfnet.nl:1990:Asd001A_OME3T_ETH-1-1-4_10:in";
    String invalidUrn = "urn:invalid";
    updateEnniPortCommand.setInboundPeer(validUrn);
    updateEnniPortCommand.setOutboundPeer(invalidUrn);
    Set<ConstraintViolation<UpdateEnniPortCommand>> constraintViolations = localValidatorFactory.validate(updateEnniPortCommand);

    assertTrue(constraintViolations.size() == 1);
    ConstraintViolation<UpdateEnniPortCommand> error = constraintViolations.iterator().next();
    assertTrue(error.getPropertyPath().toString().contains("outboundPeer"));
  }
}

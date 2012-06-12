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
package nl.surfnet.bod.mtosi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.tmforum.mtop.nrf.xsd.invdata.v1.InventoryDataType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagedElementInventoryType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagementDomainInventoryType;

import com.google.common.collect.Iterables;

public class MtosiLiveClientTestIntegration {

  private MtosiLiveClient subject;

  @Before
  public void init() throws IOException {
    Properties props = new Properties();
    props.load(new ClassPathResource("bod-default.properties").getInputStream());

    subject = new MtosiLiveClient(props.getProperty("mtosi.inventory.retrieval.endpoint"), "http://atlas.dlp.surfnet.nl");
    subject.init();
  }

  @Test
  public void retreiveInventory() {
    InventoryDataType inventory = subject.getInventory();

    assertThat(inventory, notNullValue());

    List<ManagementDomainInventoryType> mdits = inventory.getMdList().getMd();

    assertThat(mdits, hasSize(1));

    ManagementDomainInventoryType mdit = Iterables.getOnlyElement(mdits);
    List<ManagedElementInventoryType> meits = mdit.getMeList().getMeInv();

    assertThat(meits, hasSize(greaterThan(0)));
  }
}

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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MtosiLiveClientTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void convertPortName() {
    final String mtosiPortName = "/rack=1/shelf=1/slot=1/port=48";
    final String expectedPortName = "1-1-1-48";
    final String convertedPortName = new MtosiInventoryRetrievalLiveClient(null, null).convertPortName(mtosiPortName);
    assertThat(convertedPortName, equalTo(expectedPortName));
  }

  @Test
  public void convertSubPortName() {
    final String mtosiPortName = "/rack=1/shelf=1/slot=3/sub_slot=1";
    final String expectedPortName = "1-1-3-1";
    final String convertedPortName = new MtosiInventoryRetrievalLiveClient(null, null).convertPortName(mtosiPortName);
    assertThat(convertedPortName, equalTo(expectedPortName));
  }

}

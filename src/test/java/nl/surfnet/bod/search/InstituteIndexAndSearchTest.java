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
package nl.surfnet.bod.search;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nl.surfnet.bod.domain.Institute;

public class InstituteIndexAndSearchTest extends AbstractIndexAndSearch<Institute> {

  public InstituteIndexAndSearchTest() {
    super(Institute.class);
  }

  @Before
  public void setUp() {
    initEntityManager();
  }

  @After
  public void tearDown() {
    closeEntityManager();
  }

  @Test
  public void testIndexAndSearch() throws Exception {

    List<Institute> institutes = getSearchQuery("Mock_Klantnaam");
    // nothing indexed so nothing should be found
    assertThat(institutes.size(), is(0));

    index();

    institutes = getSearchQuery("Mock_Klantnaam");
    // (Mock_Klantnaam)
    assertThat(institutes.size(), is(1));

    institutes = getSearchQuery("de");
    // (De Kempel, Deltion College, Design Academy, Dienst Uitvoering Onderwijs)
    assertThat(institutes.size(), is(4));

  }

}
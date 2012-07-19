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
package nl.surfnet.bod.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReflectiveFieldComparatorTest {

  private static final int FIRST_ELEMENT = 0, SECOND_ELEMENT = 1;

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
  public void shouldNotThrowException() {
    final ReflectiveFieldComparator reflectiveFieldComparator = new ReflectiveFieldComparator(null);
    final List<?> someEmptyCollection = new ArrayList<>();
    Collections.sort(someEmptyCollection, reflectiveFieldComparator);
  }

  @Test
  public void sortNullCollectionWithInvalidFieldArgument() {
    final ReflectiveFieldComparator reflectiveFieldComparator = new ReflectiveFieldComparator("non_existing_field");
    final List<?> nullCollection = new ArrayList<>();
    nullCollection.add(null);
    nullCollection.add(null);
    
    Collections.sort(nullCollection, reflectiveFieldComparator);
    assertThat(nullCollection, hasSize(2));
  }

  @Test
  public void sortCollectionOnValidFieldArgument() {
    final ReflectiveFieldComparator reflectiveFieldComparator = new ReflectiveFieldComparator("age");
    final List<SimpleDomainObject> simpleDomainObjects = new ArrayList<>();

    simpleDomainObjects.add(new SimpleDomainObject("b", 2));
    simpleDomainObjects.add(new SimpleDomainObject("a", 1));

    assertThat(simpleDomainObjects.get(FIRST_ELEMENT).getName(), is("b"));

    
    Collections.sort(simpleDomainObjects, reflectiveFieldComparator);

    assertThat(simpleDomainObjects.get(FIRST_ELEMENT).getName(), is("a"));
    assertThat(simpleDomainObjects.get(FIRST_ELEMENT).getAge(), is(1));

    assertThat(simpleDomainObjects.get(SECOND_ELEMENT).getName(), is("b"));
    assertThat(simpleDomainObjects.get(SECOND_ELEMENT).getAge(), is(2));

  }

  @Test
  public void sortCollectionOnValidFieldArgumentWithNullElement() {
    final ReflectiveFieldComparator reflectiveFieldComparator = new ReflectiveFieldComparator("age");
    final List<SimpleDomainObject> simpleDomainObjects = new ArrayList<>();

    simpleDomainObjects.add(new SimpleDomainObject("b", 2));
    simpleDomainObjects.add(null);

    assertThat(simpleDomainObjects.get(FIRST_ELEMENT).getName(), is("b"));

    
    Collections.sort(simpleDomainObjects, reflectiveFieldComparator);

    assertNull(simpleDomainObjects.get(FIRST_ELEMENT));
    assertThat(simpleDomainObjects.get(SECOND_ELEMENT).getName(), is("b"));
    assertThat(simpleDomainObjects.get(SECOND_ELEMENT).getAge(), is(2));

  }

  @Test(expected = IllegalArgumentException.class)
  public void sortCollectionOnInvalidFieldArgument() {
    final ReflectiveFieldComparator reflectiveFieldComparator = new ReflectiveFieldComparator("iceAge");
    final List<SimpleDomainObject> simpleDomainObjects = new ArrayList<>();

    simpleDomainObjects.add(new SimpleDomainObject("b", 2));
    simpleDomainObjects.add(new SimpleDomainObject("a", 1));
    Collections.sort(simpleDomainObjects, reflectiveFieldComparator);
  }

  private static class SimpleDomainObject {

    private String name;

    private int age;

    public SimpleDomainObject(String name, int age) {
      super();
      this.name = name;
      this.age = age;
    }

    public final String getName() {
      return name;
    }

    public final int getAge() {
      return age;
    }

  }

}

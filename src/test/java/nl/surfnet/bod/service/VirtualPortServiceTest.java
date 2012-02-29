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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortServiceTest {

  @InjectMocks
  private VirtualPortService subject;

  @Mock
  private VirtualPortRepo virtualPortRepoMock;

  @Test
  public void countShouldCount() {
    when(virtualPortRepoMock.count()).thenReturn(2L);

    long count = subject.count();

    assertThat(count, is(2L));
  }

  @Test
  public void delete() {
    VirtualPort virtualPort = new VirtualPortFactory().create();

    subject.delete(virtualPort);

    verify(virtualPortRepoMock).delete(virtualPort);
  }

  @Test
  public void update() {
    VirtualPort virtualPort = new VirtualPortFactory().create();

    subject.update(virtualPort);

    verify(virtualPortRepoMock).save(virtualPort);
  }

  @Test
  public void findAll() {
    VirtualPort port = new VirtualPortFactory().create();
    when(virtualPortRepoMock.findAll()).thenReturn(Lists.newArrayList(port));

    List<VirtualPort> ports = subject.findAll();

    assertThat(ports, contains(port));
  }

  @Test(expected = IllegalArgumentException.class)
  public void findEntriesWithMaxResultZeroShouldGiveAnException() {
    subject.findEntries(1, 0);
  }

  @Test
  public void findEntries() {
    VirtualPort port = new VirtualPortFactory().create();

    when(virtualPortRepoMock.findAll(any(PageRequest.class))).thenReturn(new PageImpl<VirtualPort>(Lists.newArrayList(port)));

    List<VirtualPort> ports = subject.findEntries(5, 10);

    assertThat(ports, contains(port));
  }

  @Test
  public void findAllForUserWithoutGroupsShouldNotGoToRepo() {
      List<VirtualPort> ports = subject.findAllForUser(new RichUserDetailsFactory().create());

      assertThat(ports, hasSize(0));
      verifyZeroInteractions(virtualPortRepoMock);
  }

  @Test
  public void findAllEntriesForUserWithoutGroupsShouldNotGoToRepo() {
      List<VirtualPort> ports = subject.findEntriesForUser(new RichUserDetailsFactory().create(), 2, 5, new Sort("id"));

      assertThat(ports, hasSize(0));
      verifyZeroInteractions(virtualPortRepoMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void findAllForUser() {
    when(virtualPortRepoMock.findAll(any(Specification.class))).thenReturn(ImmutableList.of(new VirtualPortFactory().create()));

    List<VirtualPort> ports = subject.findAllForUser(new RichUserDetailsFactory().addUserGroup("urn:mygroup").create());

    assertThat(ports, hasSize(1));
  }

}

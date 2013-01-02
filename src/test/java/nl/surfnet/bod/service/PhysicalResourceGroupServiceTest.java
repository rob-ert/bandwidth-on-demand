/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.repo.ActivationEmailLinkRepo;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.UserGroupFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalResourceGroupServiceTest {

  @InjectMocks
  private PhysicalResourceGroupService subject;

  @Mock
  private PhysicalResourceGroupRepo groupRepoMock;

  @Mock
  private InstituteIddService instituteServiceMock;

  @Mock
  private ActivationEmailLinkRepo activationEmailLinkRepoMock;

  @Mock
  private EmailSender emailSender;

  private Institute instituteOne = new InstituteFactory().setId(1L).setName("oneInst").create();
  private Institute instituteTwo = new InstituteFactory().setId(2L).setName("twoInst").create();
  private PhysicalResourceGroup physicalResourceGroupOne = new PhysicalResourceGroupFactory()
      .setInstitute(instituteOne).create();
  private PhysicalResourceGroup physicalResourceGroupTwo = new PhysicalResourceGroupFactory()
      .setInstitute(instituteTwo).create();
  private List<PhysicalResourceGroup> physicalResourceGroups = ImmutableList.of(physicalResourceGroupOne,
      physicalResourceGroupTwo);


  @Test
  public void shoudFillInstitutesFindAll() {
    when(groupRepoMock.findAll()).thenReturn(physicalResourceGroups);
    when(instituteServiceMock.find(1L)).thenReturn(instituteOne);
    when(instituteServiceMock.find(2L)).thenReturn(instituteTwo);

    List<PhysicalResourceGroup> prgs = subject.findAll();

    Iterator<PhysicalResourceGroup> it = prgs.iterator();
    assertThat(it.next().getInstitute(), is(instituteOne));
    assertThat(it.next().getInstitute(), is(instituteTwo));
  }

  @Test
  public void shoudFillInstitutesFindEntries() {
    Page<PhysicalResourceGroup> pageResult = new PageImpl<PhysicalResourceGroup>(physicalResourceGroups);
    Sort sort = new Sort("id");

    when(groupRepoMock.findAll(new PageRequest(1, 1, sort))).thenReturn(pageResult);
    when(instituteServiceMock.find(1L)).thenReturn(instituteOne);
    when(instituteServiceMock.find(2L)).thenReturn(instituteTwo);

    List<PhysicalResourceGroup> prgs = subject.findEntries(1, 1, new Sort("id"));

    Iterator<PhysicalResourceGroup> it = prgs.iterator();
    assertThat(it.next().getInstitute(), is(instituteOne));
    assertThat(it.next().getInstitute(), is(instituteTwo));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldFillInstitutesFindAllForAdminGroups() {
    UserGroup userGroupOne = new UserGroupFactory().setId("oneUser").create();
    UserGroup userGroupTwo = new UserGroupFactory().setId("twoUser").create();

    Collection<UserGroup> groups = ImmutableList.of(userGroupOne, userGroupTwo);

    when(groupRepoMock.findByAdminGroupIn(anyCollection())).thenReturn(physicalResourceGroups);
    when(instituteServiceMock.find(1L)).thenReturn(instituteOne);
    when(instituteServiceMock.find(2L)).thenReturn(instituteTwo);

    Collection<PhysicalResourceGroup> prgs = subject.findAllForAdminGroups(groups);

    Iterator<PhysicalResourceGroup> it = prgs.iterator();
    assertThat(it.next().getInstitute(), is(instituteOne));
    assertThat(it.next().getInstitute(), is(instituteTwo));
  }

  @Test
  public void createActivationEmailLink() {
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();

    when(activationEmailLinkRepoMock.save(any(ActivationEmailLink.class))).thenAnswer(
        new Answer<ActivationEmailLink>() {
          @Override
          public ActivationEmailLink answer(InvocationOnMock invocation) throws Throwable {
            return (ActivationEmailLink) invocation.getArguments()[0];
          }
        });

    ActivationEmailLink link = subject.sendActivationRequest(prg);

    verify(emailSender).sendActivationMail(link);
    verify(activationEmailLinkRepoMock, atLeastOnce()).save(link);
  }

}
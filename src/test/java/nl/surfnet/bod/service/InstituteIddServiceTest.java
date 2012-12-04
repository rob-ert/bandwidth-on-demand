/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.idd.IddClient;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.repo.InstituteRepo;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.KlantenFactory;

@RunWith(MockitoJUnitRunner.class)
public class InstituteIddServiceTest {

  @InjectMocks
  private InstituteIddService subject;

  @Mock
  private IddClient iddClientMock;

  @Mock
  private InstituteRepo instituteRepoMock;

  @Mock
  private LogEventService logEventService;
  
  @Mock
  private SnmpAgentService snmpAgentService;

  @Test
  public void whenAllInstituesEqualDontUpdate() {
    List<Institute> institutes = ImmutableList.of(
        new InstituteFactory().setId(1L).setName("SURFnet").setShortName("SURF").create(), new InstituteFactory()
            .setId(2L).setName("Zilverline").setShortName("Z").create());
    List<Klanten> klanten = ImmutableList.of(new KlantenFactory().setKlantid(1).setKlantnaam("SURFnet")
        .setKlantafkorting("SURF").create(), new KlantenFactory().setKlantid(2).setKlantnaam("Zilverline")
        .setKlantafkorting("Z").create());

    when(instituteRepoMock.findByAlignedWithIDD(true)).thenReturn(institutes);
    when(iddClientMock.getKlanten()).thenReturn(klanten);

    subject.refreshInstitutes();

    verify(instituteRepoMock, times(2)).save(eq(Collections.<Institute> emptyList()));
  }

  @Test
  public void whenAKlantHasChangedUpdateIt() {
    List<Institute> institutes = ImmutableList.of(
        new InstituteFactory().setId(1L).setName("SURFnet").setShortName("SURF").create(), new InstituteFactory()
            .setId(2L).setName("Zilverline").setShortName("Z").create());
    List<Klanten> klanten = ImmutableList.of(new KlantenFactory().setKlantid(1).setKlantnaam("SURFnet")
        .setKlantafkorting("SURF").create(), new KlantenFactory().setKlantid(2).setKlantnaam("Zilverline BV")
        .setKlantafkorting("Z").create());

    when(instituteRepoMock.findByAlignedWithIDD(true)).thenReturn(institutes);
    when(iddClientMock.getKlanten()).thenReturn(klanten);

    subject.refreshInstitutes();

    verify(instituteRepoMock).save(
        argThat(contains(new InstituteFactory().setId(2L).setName("Zilverline").setShortName("Z").create())));
    verify(instituteRepoMock).save(
        argThat(contains(new InstituteFactory().setId(2L).setName("Zilverline BV").setShortName("Z").create())));

    verify(instituteRepoMock).findByAlignedWithIDD(true);
    verifyNoMoreInteractions(instituteRepoMock);
  }

  @Test
  public void whenAKlantIsRemovedFromIddItShouldBeUnAlligned() {
    List<Institute> institutes = ImmutableList.of(
        new InstituteFactory().setId(1L).setName("SURFnet").setShortName("SURF").setAlignedWithIDD(true).create(),
        new InstituteFactory().setId(2L).setName("Zilverline").setShortName("Z").setAlignedWithIDD(true).create());
    List<Klanten> klanten = ImmutableList.of(new KlantenFactory().setKlantid(1).setKlantnaam("SURFnet")
        .setKlantafkorting("SURF").create());

    when(instituteRepoMock.findByAlignedWithIDD(true)).thenReturn(institutes);
    when(iddClientMock.getKlanten()).thenReturn(klanten);

    subject.refreshInstitutes();

    verify(instituteRepoMock).save(
        argThat(contains(new InstituteFactory().setId(2L).setName("Zilverline").setShortName("Z").create())));
    verify(instituteRepoMock).save(eq(Collections.<Institute> emptyList()));

    verify(instituteRepoMock).findByAlignedWithIDD(true);
    verifyNoMoreInteractions(instituteRepoMock);

    assertThat(institutes.get(1).isAlignedWithIDD(), is(false));
  }

  @Test
  public void whenAnUnalignedInstituteReappearsAgainInIDDItShouldBeAligned() {
    List<Institute> institutes = ImmutableList.of(new InstituteFactory().setId(1L).setName("SURFnet")
        .setShortName("SURF").setAlignedWithIDD(true).create());

    List<Klanten> klanten = ImmutableList.of(new KlantenFactory().setKlantid(1).setKlantnaam("SURFnet")
        .setKlantafkorting("SURF").create(), new KlantenFactory().setKlantid(2).setKlantnaam("Zilverline")
        .setKlantafkorting("Z").create());

    when(instituteRepoMock.findByAlignedWithIDD(true)).thenReturn(institutes);
    when(iddClientMock.getKlanten()).thenReturn(klanten);

    subject.refreshInstitutes();

    verify(instituteRepoMock).save(eq(Collections.<Institute> emptyList()));

    //TODO setAlignedWithIDD Attribute is not matched
    verify(instituteRepoMock).save(
        argThat(contains(new InstituteFactory().setId(2L).setName("Zilverline").setShortName("Z")
            .setAlignedWithIDD(true).create())));

    verify(instituteRepoMock).findByAlignedWithIDD(true);
    verifyNoMoreInteractions(instituteRepoMock);

    assertThat(institutes.get(0).isAlignedWithIDD(), is(true));
  }

}

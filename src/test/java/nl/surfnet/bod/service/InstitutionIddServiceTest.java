package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.Institution;
import nl.surfnet.bod.extern.IddClient;
import nl.surfnet.bod.idd.Klanten;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class InstitutionIddServiceTest {

  private IddClient iddClientMock = mock(IddClient.class);

  private InstitutionIddService subject = new InstitutionIddService();

  @Before
  public void setUp() {
    subject.setIddClient(iddClientMock);
  }

  @Test
  public void shouldIgnoreEmptyNames() {
    Klanten klant = newKlantWithName("");

    when(iddClientMock.getKlanten()).thenReturn(Lists.newArrayList(klant));

    Collection<Institution> institutions = subject.getInstitutions();

    assertThat(institutions, hasSize(0));
  }

  @Test
  public void shouldRemoveTrailingWhitespaceFromName() {
    Klanten klant = newKlantWithName("SURFnet\n");

    when(iddClientMock.getKlanten()).thenReturn(Lists.newArrayList(klant));

    Collection<Institution> institutions = subject.getInstitutions();

    assertThat(institutions, hasSize(1));
    assertThat(institutions.iterator().next().getName(), is("SURFnet"));
  }

  private Klanten newKlantWithName(String naam) {
    Klanten klanten = new Klanten();
    klanten.setKlantnaam(naam);
    return klanten;
  }
}

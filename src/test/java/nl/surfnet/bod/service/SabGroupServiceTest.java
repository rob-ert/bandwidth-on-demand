package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.sabng.SabNgEntitlementsHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class SabGroupServiceTest {

  private final static String SURFNET = "SURFnet";
  private final static String WESAIDSO = "WeSaidSo";
  private final static String NAME_ID = "testName";

  @InjectMocks
  private SabGroupService subject;

  @Mock
  private SabNgEntitlementsHandler sabNgEntitlementsHandlerMock;

  @Test
  public void shouldAddSabGroups() {

    List<String> institutes = Lists.newArrayList(SURFNET, WESAIDSO);
    when(sabNgEntitlementsHandlerMock.checkInstitutes(eq(NAME_ID))).thenReturn(institutes);

    Collection<UserGroup> groups = subject.getGroups(NAME_ID);
    assertThat(groups, hasSize(2));
    assertThat(groups, contains(
        new UserGroup(subject.composeGroupName(SURFNET),
            subject.composeName(SURFNET),
            subject.composeDescription(SURFNET)),
        new UserGroup(subject.composeGroupName(WESAIDSO),
            subject.composeName(WESAIDSO),
            subject.composeDescription(WESAIDSO))));
  }

  @Test
  public void shouldNotAddGroups() {
    Collection<UserGroup> groups = subject.getGroups(NAME_ID);
    assertThat(groups, hasSize(0));
  }

  @Test
  public void shouldComposeGroupName() {
    assertThat(subject.composeGroupName(NAME_ID), is(SabGroupService.GROUP_PREFIX + NAME_ID.toLowerCase()));
  }

  @Test
  public void shouldComposeName() {
    assertThat(subject.composeName(NAME_ID), is(SabGroupService.NAME_PREFIX + NAME_ID));
  }

  @Test
  public void shouldComposeDescription() {
    assertThat(subject.composeDescription(NAME_ID), is(SabGroupService.DESCRIPTION_PREFIX + NAME_ID));
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowOnNullArgumentInComposeGroupName() {
    subject.composeGroupName(null);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowOnNullArgumentInComposeName() {
    subject.composeName(null);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowOnNullArgumentInComposeDescription() {
    subject.composeDescription(null);
  }

}

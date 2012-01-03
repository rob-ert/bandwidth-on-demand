package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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

}

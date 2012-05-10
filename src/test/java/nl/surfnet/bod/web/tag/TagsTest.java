package nl.surfnet.bod.web.tag;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class TagsTest {

  @Test
  public void createUrlShouldPrependTheContextPath() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getContextPath()).thenReturn("/bod");

    String url = Tags.createUrl("/css/main.css", request);

    assertThat(url, is("/bod/css/main.css"));
  }

}

package nl.surfnet.bod.web.csrf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;

public class CsrfHandlerInterceptorTest {

  private CsrfHandlerInterceptor subject = new CsrfHandlerInterceptor();

  @Test
  public void getReqeustsShouldBeIgnored() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getMethod()).thenReturn("GET", "get");

    boolean result = subject.preHandle(request, response, new Object());
    assertThat(result, is(true));

    result = subject.preHandle(request, response, new Object());
    assertThat(result, is(true));
  }

  @Test
  public void changingRequestsShouldChecked() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    HttpSession session = mock(HttpSession.class);

    String[] methods = { "post", "DELETE", "delete", "PUT", "put" };

    when(request.getMethod()).thenReturn("POST", methods);
    when(request.getSession()).thenReturn(session);

    for (int i = 0; i < methods.length + 1; i++) {
      boolean result = subject.preHandle(request, response, new Object());
      assertThat(result, is(false));
    }
  }

}

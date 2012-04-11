package nl.surfnet.bod.web.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class BodTagSupport extends SimpleTagSupport {

  protected HttpServletRequest getRequest() {
    return (HttpServletRequest) ((PageContext) getJspContext()).getRequest();
  }
}

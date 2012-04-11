package nl.surfnet.bod.web.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class BodTagSupport extends SimpleTagSupport {

  protected PageContext getPageContext() {
    return (PageContext) getJspContext();
  }

  protected HttpServletRequest getRequest() {
    return (HttpServletRequest) getPageContext().getRequest();
  }
}

package nl.surfnet.bod.web.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import nl.surfnet.bod.web.csrf.CsrfTokenManager;

public class CsrfTokenTag extends BodTagSupport {

  @Override
  public void doTag() throws JspException, IOException {
    getJspContext().getOut().write(
        "<output id=\"csrf-token\">" + CsrfTokenManager.getTokenForSession(getSession()) + "</output>");
  }
}

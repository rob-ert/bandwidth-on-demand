package nl.surfnet.bod.web.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

public class CssTag extends BodTagSupport {

  private static final String TEMPLATE = "<link  href=\"%s\" rel=\"stylesheet\" type=\"text/css\" media=\"screen\" />";

  private String value;

  @Override
  public void doTag() throws JspException, IOException {
    getJspContext().getOut().write(String.format(TEMPLATE, createUrl()));
  }

  private String createUrl() {
    return Tags.createUrl(value, getRequest());
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}

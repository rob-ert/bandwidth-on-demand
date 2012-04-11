package nl.surfnet.bod.web.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

public class ScriptTag extends BodTagSupport {

  private static final String TEMPLATE = "<script src=\"%s\" type=\"text/javascript\"></script>";

  private String value;

  @Override
  public void doTag() throws JspException, IOException {
    getJspContext().getOut().write(String.format(TEMPLATE, createUrl()));
  }

  private Object createUrl() {
    return Tags.createUrl(value, getRequest());
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}

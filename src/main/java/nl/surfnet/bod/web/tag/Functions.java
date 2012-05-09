package nl.surfnet.bod.web.tag;

import org.springframework.util.StringUtils;

public final class Functions {

  private Functions() {
  }

  public static String translateNewLineBr(String input) {
    return StringUtils.replace(input, "\n", "<br/>");
  }
}

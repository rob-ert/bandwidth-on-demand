package nl.surfnet.bod.web.tag;

import org.springframework.util.StringUtils;

public class Functions {

  public static String translateNewLineBr(String input) {
    return StringUtils.replace(input, "\n", "<br/>");
  }
}

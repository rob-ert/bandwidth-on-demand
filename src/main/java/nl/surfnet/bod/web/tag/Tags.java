package nl.surfnet.bod.web.tag;

import javax.servlet.http.HttpServletRequest;

public final class Tags {

  private Tags() {
  }

  public static String createUrl(String url, HttpServletRequest request) {
    return request.getContextPath() + url;
  }
}

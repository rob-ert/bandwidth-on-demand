package nl.surfnet.bod.web;

public final class WebUtils {

  public static final String CREATE = "create";
  public static final String SHOW = "show";
  public static final String EDIT = "edit";
  public static final String UPDATE = "update";
  public static final String DELETE = "delete";
  public static final String LIST="list";

  public static final int MAX_ITEMS_PER_PAGE = 15;

  private WebUtils() {
  }

  public static int calculateFirstPage(Integer page) {
    return page == null ? 0 : (page.intValue() - 1) * MAX_ITEMS_PER_PAGE;
  }

  public static int calculateMaxPages(long totalEntries) {
    float nrOfPages = (float) totalEntries / MAX_ITEMS_PER_PAGE;
    return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
  }
}

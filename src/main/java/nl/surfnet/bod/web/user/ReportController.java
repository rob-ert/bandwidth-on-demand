package nl.surfnet.bod.web.user;

import java.util.List;

import nl.surfnet.bod.web.base.AbstractReportController;
import nl.surfnet.bod.web.security.Security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Lists;

@Controller("userReportController")
@RequestMapping(ReportController.PAGE_URL)
public class ReportController extends AbstractReportController {
  public static final String PAGE_URL = "report";

  @Override
  protected String getPageUrl() {
    return PAGE_URL;
  }

  @Override
  protected List<String> getAdminGroups() {
    if (Security.isSelectedUserRole()) {
      return Lists.newArrayList("dummygroup");
    }

    throw new IllegalStateException("User has no user role");
  }
}

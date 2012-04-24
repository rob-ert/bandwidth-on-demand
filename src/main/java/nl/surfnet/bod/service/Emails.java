package nl.surfnet.bod.service;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.common.base.Throwables;

public final class Emails {

  public static class ActivationEmail {
    private static final String ACTIVATION_BODY = //
    "Dear ICT Manager,\n\n" //
        + "Please click the link to activate this email adres for physical resource group: %s\n\n" + "Kind regards,\n" //
        + "The bandwidth on Demand Application team";

    public static String body(String link) {
      return String.format(ACTIVATION_BODY, link);
    }

    public static String subject(String prg) {
      return String.format("[BoD] Activation mail for Physical Resource Group %s", prg);
    }
  }

  public static class VirtualPortRequestMail {
    private static final String VIRTUAL_PORT_REQUEST_BODY = //
    "Dear ICT Manager,\n\n" //
        + "You have received a new Virtual Port Request.\n\n" //
        + "Who: %s (%s)\n" //
        + "Physical Resource Group: %s\n" //
        + "Virtual Resource Group: %s\n" //
        + "Minimum Bandwidth: %d Mbit/s\n" //
        + "Reason: %s\n\n" //
        + "Click on the following link %s to create the virtual port.\n\n" //
        + "Kind regards,\n" //
        + "The Bandwidth on Demand Application team";

    public static String body(RichUserDetails from, VirtualPortRequestLink requestLink, String link) {
      return String.format(VIRTUAL_PORT_REQUEST_BODY, from.getDisplayName(), from.getEmail(), requestLink
          .getPhysicalResourceGroup().getInstitute().getName(), requestLink.getVirtualResourceGroup().getName(),
          requestLink.getMinBandwidth(), requestLink.getMessage(), link);
    }

    public static String subject(String institute) {
      return String.format("[BoD] A Virtual Port Request for %s", institute);
    }
  }

  public static class ErrorMail {
    private static final String ERROR_MAIL_BODY = //
    "Dear BoD Team,\n\n" //
        + "An exception occured.\n\n" //
        + "User: %s (%s)\n" //
        + "Username: %s\n" //
        + "Request: %s (%s)\n" //
        + "Around: %s\n" //
        + "Stacktrace:\n%s\n";

    public static String subject(String envUrl, Throwable throwable) {
      return String.format("[Exception on %s] %s", envUrl, throwable.getMessage());
    }

    public static String body(RichUserDetails user, Throwable throwable, HttpServletRequest request) {
      return String.format(ERROR_MAIL_BODY,
          user.getDisplayName(),
          user.getEmail(),
          user.getUsername(),
          request.getRequestURL().append(request.getQueryString() != null ? "?" + request.getQueryString() : "").toString(),
          request.getMethod(),
          DateTimeFormat.mediumDateTime().print(DateTime.now()),
          Throwables.getStackTraceAsString(throwable));
    }
  }

}

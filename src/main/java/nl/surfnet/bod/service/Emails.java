package nl.surfnet.bod.service;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.common.base.Throwables;

public final class Emails {

  private static final String FOOTER = //
      "Kind regards,\n" //
    + "The Bandwidth on Demand Application team";

  public static class ActivationEmail {
    private static final String ACTIVATION_BODY = //
        "Dear ICT Administrator,\n\n" //
      + "Please click the link to activate the email address for institute: %s\n\n" //
      + FOOTER;

    public static String body(String link) {
      return String.format(ACTIVATION_BODY, link);
    }

    public static String subject(String prg) {
      return String.format("[BoD] Activate email of %s", prg);
    }
  }

  public static class VirtualPortRequestMail {
    private static final String VIRTUAL_PORT_REQUEST_BODY = //
        "Dear ICT Administrator,\n\n" //
      + "You have received a new Virtual Port Request.\n\n" //
      + "From: %s (%s)\n" //
      + "Team: %s\n" //
      + "Minimum Bandwidth: %d Mbit/s\n" //
      + "Reason: %s\n" //
      + "Institute: %s\n\n" //
      + "Click on the following link %s to create the virtual port.\n\n" //
      + FOOTER;

    public static String body(RichUserDetails from, VirtualPortRequestLink requestLink, String link) {
      return String.format(
          VIRTUAL_PORT_REQUEST_BODY,
          from.getDisplayName(), from.getEmail(), requestLink.getVirtualResourceGroup().getName(),
          requestLink.getMinBandwidth(), requestLink.getMessage(), requestLink
          .getPhysicalResourceGroup().getInstitute().getName(), link);
    }

    public static String subject(RichUserDetails user) {
      return String.format("[BoD] Virtual Port Request from %s", user.getDisplayName());
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
      + "Stacktrace:\n%s\n\n"
      + FOOTER;

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

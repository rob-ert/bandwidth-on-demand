/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.service;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;

public final class Emails {

  private static final String FOOTER = //
      "\n\nKind regards,\n" //
      + "The Bandwidth on Demand Application team";

  public static class ActivationEmail {
    private static final String ACTIVATION_BODY = //
    "Dear BoD Administrator,\n\n" //
        + "Please click the link to activate the email address for institute: %s" //
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
    "Dear BoD Administrator,\n\n" //
        + "You have received a new Virtual Port Request.\n\n" //
        + "From: %s (%s)\n" //
        + "Team: %s\n" //
        + "Preferred name: %s\n" //
        + "Minimum Bandwidth: %d Mbit/s\n" //
        + "Reason: %s\n" //
        + "Institute: %s\n\n" //
        + "Click on the following link %s to create the virtual port." //
        + FOOTER;

    public static String body(RichUserDetails from, VirtualPortRequestLink requestLink, String link) {
      return String.format(VIRTUAL_PORT_REQUEST_BODY, from.getDisplayName(), from.getEmail(), requestLink
          .getVirtualResourceGroup().getName(), requestLink.getUserLabel(), requestLink.getMinBandwidth(),
          requestLink.getMessage(), requestLink.getPhysicalResourceGroup().getInstitute().getName(), link);
    }

    public static String subject(RichUserDetails user) {
      return String.format("[BoD] Virtual Port Request from %s", user.getDisplayName());
    }
  }

  public static final class VirtualPortRequestApproveMail {
    private static final String BODY = //
    "Dear %s,\n\n" //
        + "Your Request for a Virtual Port from %s for your team %s has been approved.\n" //
        + "The Virtual Port is now available with the name '%s'. It has a max. bandwidth of %d Mbit/s." //
        + FOOTER;

    public static String body(VirtualPortRequestLink link, VirtualPort port) {
      return String.format(BODY, link.getRequestorName(), link.getPhysicalResourceGroup().getName(), link
          .getVirtualResourceGroup().getName(), port.getUserLabel(), port.getMaxBandwidth());
    }

    public static String subject(VirtualPort port) {
      return String.format("[BoD] Virtual Port %s created", port.getUserLabel());
    }
  }

  public static class VirtualPortRequestDeclineMail {
    private static final String BODY = //
        "Dear %s,\n\n" //
        + "You Request for a Virtual Port from %s for your team %s has been declined.\n" //
        + "The reason the BoD Administrator gave:\n%s"
        + FOOTER;

    public static String subject() {
      return "[BoD] Your Virtual Port Request has been declined";
    }

    public static String body(VirtualPortRequestLink link, String message) {
      return String.format(BODY,
          link.getRequestorName(), link.getPhysicalResourceGroup().getName(), link.getVirtualResourceGroup().getName(),
          message);
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
        + "Stacktrace:\n%s" + FOOTER;

    public static String subject(String envUrl, Throwable throwable) {
      return String.format("[Exception on %s] %s", envUrl, throwable.getMessage());
    }

    public static String body(RichUserDetails user, Throwable throwable, HttpServletRequest request) {
      Optional<RichUserDetails> optUser = Optional.fromNullable(user);
      return String.format(ERROR_MAIL_BODY,

          getOrUnknown(new Function<RichUserDetails, String>() {
            @Override
            public String apply(RichUserDetails input) {
              return input.getDisplayName();
            }
          }, optUser),

          getOrUnknown(new Function<RichUserDetails, String>() {
            @Override
            public String apply(RichUserDetails input) {
              return input.getEmail().or("Email not known");
            }
          }, optUser),

          getOrUnknown(new Function<RichUserDetails, String>() {
            @Override
            public String apply(RichUserDetails input) {
              return input.getUsername();
            }
          }, optUser),

          request.getRequestURL().append(request.getQueryString() != null ? "?" + request.getQueryString() : "").toString(),

          request.getMethod(), DateTimeFormat.mediumDateTime().print(DateTime.now()),

          Throwables.getStackTraceAsString(throwable));
    }

    private static String getOrUnknown(Function<RichUserDetails, String> function, Optional<RichUserDetails> user) {
      return user.transform(function).or("Unknown");
    }
  }

}

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
package nl.surfnet.bod.util;

import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Environment {

  public static final DateTimeZone DEFAULT_TIME_ZONE = DateTimeZone.getDefault();;

  @Value("${shibboleth.imitate}")
  private boolean imitateShibboleth;

  @Value("${shibboleth.imitate.displayName}")
  private String imitateShibbolethDisplayName;

  @Value("${shibboleth.imitate.userId}")
  private String imitateShibbolethUserId;

  @Value("${shibboleth.logout.url}")
  private String shibbolethLogoutUrl;

  @Value("${shibboleth.imitate.email}")
  private String imitateShibbolethEmail;

  @Value("${os.url}")
  private String openSocialUrl;

  @Value("${os.oauth-key}")
  private String openSocialOAuthKey;

  @Value("${os.oauth-secret}")
  private String openSocialOAuthSecret;

  @Value("${external.bod.url}")
  private String externalBodUrl;

  @Value("${bod.development}")
  private boolean development;

  @Value("${bod.version}")
  private String version;

  @Value("${google.analytics.code}")
  private String googleAnalyticsCode;

  @Value("${os.group.noc}")
  private String nocGroup;

  public Environment() {
  }

  public Environment(String openSocialUrl, String openSocialOAuthKey, String openSocialOAuthSecret) {
    this.openSocialUrl = openSocialUrl;
    this.openSocialOAuthKey = openSocialOAuthKey;
    this.openSocialOAuthSecret = openSocialOAuthSecret;
  }

  public Environment(boolean imitateShibboleth, String imitateShibbolethUserId, String imitateShibbolethDisplayName,
      String imitateShibbolethEmail, String shibbolethLogoutUrl) {
    this.imitateShibboleth = imitateShibboleth;
    this.imitateShibbolethUserId = imitateShibbolethUserId;
    this.imitateShibbolethDisplayName = imitateShibbolethDisplayName;
    this.imitateShibbolethEmail = imitateShibbolethEmail;
    this.shibbolethLogoutUrl = shibbolethLogoutUrl;
  }

  public String getOpenSocialUrl() {
    return openSocialUrl;
  }

  public String getOpenSocialOAuthKey() {
    return openSocialOAuthKey;
  }

  public String getOpenSocialOAuthSecret() {
    return openSocialOAuthSecret;
  }

  public boolean getImitateShibboleth() {
    return imitateShibboleth;
  }

  public String getImitateShibbolethDisplayName() {
    return imitateShibbolethDisplayName;
  }

  public String getImitateShibbolethUserId() {
    return imitateShibbolethUserId;
  }

  public String getImitateShibbolethEmail() {
    return imitateShibbolethEmail;
  }

  public String getShibbolethLogoutUrl() {
    return shibbolethLogoutUrl;
  }

  public String getExternalBodUrl() {
    return externalBodUrl;
  }

  public boolean isDevelopment() {
    return development;
  }

  public void setDevelopment(boolean development) {
    this.development = development;
  }

  public String getGoogleAnalyticsCode() {
    return googleAnalyticsCode;
  }

  public void setGoogleAnalyticsCode(String googleAnalyticsCode) {
    this.googleAnalyticsCode = googleAnalyticsCode;
  }

  public String getVersion() {
    return version;
  }

  public String getNocGroup() {
    return nocGroup;
  }

  public String getDefaultTimeZoneId() {
    return DEFAULT_TIME_ZONE.getID();
  }
}

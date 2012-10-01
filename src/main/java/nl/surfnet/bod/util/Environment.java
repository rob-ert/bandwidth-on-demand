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

  @Value("${oauth.server.url}")
  private String oauthServerUrl;

  @Value("${oauth.admin.clientId}")
  private String adminClientId;

  @Value("${oauth.admin.secret}")
  private String adminSecret;

  @Value("${oauth.client.clientId}")
  private String clientClientId;

  @Value("${oauth.client.secret}")
  private String clientSecret;

  @Value("${oauth.resource.key}")
  private String resourceKey;

  @Value("${oauth.resource.secret}")
  private String resourceSecret;

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
    return DateTimeZone.getDefault().getID();
  }

  public String getDefaultTimeZoneOffset() {
    final int offsetInMillis = DateTimeZone.getDefault().getOffset(null);

    final int offsetHours = offsetInMillis / (60 * 60 * 1000);
    final int offsetMinutes = offsetInMillis % offsetHours;

    return String.format("UTC %0+3d:%02d", offsetHours, offsetMinutes);
  }

  public String getAdminClientId() {
    return adminClientId;
  }

  public String getAdminSecret() {
    return adminSecret;
  }

  public String getClientClientId() {
    return clientClientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getResourceKey() {
    return resourceKey;
  }

  public String getResourceSecret() {
    return resourceSecret;
  }

  public String getOauthServerUrl() {
    return oauthServerUrl;
  }

  public void setOauthServerUrl(String oauthServerUrl) {
    this.oauthServerUrl = oauthServerUrl;
  }

  public void setResourceKey(String resourceKey) {
    this.resourceKey = resourceKey;
  }

  public void setResourceSecret(String resourceSecret) {
    this.resourceSecret = resourceSecret;
  }
}

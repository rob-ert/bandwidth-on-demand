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
import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.jdbc.Predef._
import akka.util.duration._

class RequestAccessTokenSimulation extends Simulation {

  def apply = {

    val baseUrl = "http://localhost:8082/bod"
    val httpConf = httpConfig.baseURL(baseUrl)

    val oauthServer = "http://localhost:8080"

    val loginFeeder = Iterator(
        Map("nameId" -> "urn:kees", "displayName" -> "Kees"),
        Map("nameId" -> "urn:henk", "displayName" -> "Henk"),
        Map("nameId" -> "urn:truus", "displayName" -> "Truus"),
        Map("nameId" -> "urn:joop", "displayName" -> "Joop"),
        Map("nameId" -> "urn:klaas", "displayName" -> "Klaas"),
        Map("nameId" -> "urn:koos", "displayName" -> "Koos"),
        Map("nameId" -> "urn:lies", "displayName" -> "Lies"),
        Map("nameId" -> "urn:aad", "displayName" -> "Aad"),
        Map("nameId" -> "urn:clazina", "displayName" -> "Clazina"),
        Map("nameId" -> "urn:frits", "displayName" -> "Frits"))

    // this scenario requires that the bod-authorization server runs with a form login
    val scn = scenario("Request and revoke an OAuht2 Access Token for NSI")
      .feed(loginFeeder)
      .exec(
        http("User Dashboard").get("/user")
          .queryParam("nameId", "${nameId}")
          .queryParam("displayName", "${displayName}")
      )
      .pause(0.5 seconds, 2 seconds)
      .exec(
        http("NSI OAuth tab").get("/oauth2/tokens")
      )
      .pause(0.5 seconds, 2 seconds)
      .exec(
        http("Request access token").get("/oauth2/token")
          .check(xpath("//input[@name='AUTH_STATE']/@value").find.saveAs("authState"))
      )
      .pause(0.5 seconds, 2 seconds)
      .exec(
        http("Login at OAuthServer").post(oauthServer + "/oauth2/authorize")
          .param("username", "${nameId}")
          .param("password", "")
          .param("AUTH_STATE", "${authState}")
          .check(regex("""This data will be shared""").exists)
      )
      .pause(0.5 seconds, 2 seconds)
      .exec(
        http("Give consent").post(oauthServer + "/oauth2/consent")
          .multiValuedParam("GRANTED_SCOPES", List("release", "reserve"))
          .param("user_oauth_approval", "true")
          .param("AUTH_STATE", "${authState}")
          .check(regex("""tokenId=(\d+)"""").find.saveAs("tokenId"))
          .check(css("output#csrf-token").find.saveAs("csrf-token"))
      )
      .pause(0.5 seconds, 2 seconds)
      .exec(
        http("Revoke acces token").post("/oauth2/token/delete")
          .param("_method", "DELETE")
          .param("tokenId", "${tokenId}")
          .param("csrf-token", "${csrf-token}")
      )

    List(scn.configure.protocolConfig(httpConf).users(10).ramp(2 seconds))
  }
}

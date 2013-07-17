/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class RequestAccessTokenSimulation extends Simulation {

  val baseUrl = "http://localhost:8082/bod"
  val httpConf = http.baseURL(baseUrl)

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
        .check(
          regex("""tokenId=(\d+)"""").find.saveAs("tokenId"),
          css("output#csrf-token").find.saveAs("csrf-token"))
    )
    .pause(0.5 seconds, 2 seconds)
    .exec(
      http("Revoke acces token").post("/oauth2/token/delete")
        .param("_method", "DELETE")
        .param("tokenId", "${tokenId}")
        .param("csrf-token", "${csrf-token}")
    )

  setUp(scn.inject(ramp( 10 users) over(2 seconds))).protocols(httpConf)
}

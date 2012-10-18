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

package old

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import akka.util.duration._

class CreateReservationSimulation extends Simulation {
  val urlBase = "http://localhost:8082"

  val httpConf = httpConfig.baseURL(urlBase)

  val headers_1 = Map(
    "Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
    "Accept-Encoding" -> """gzip, deflate""",
    "Accept-Language" -> """en-us""",
    "Connection" -> """keep-alive""",
    "Host" -> """localhost:8082""",
    "User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10"""
  )

  val headers_2 = headers_1 ++ Map(
    "Referer" -> """http://localhost:8082/bod/"""
  )

  val headers_3 = headers_1 ++ Map(
    "Referer" -> """http://localhost:8082/bod/virtualports/request"""
  )

  val headers_4 = Map(
    "Accept" -> """application/json, text/javascript, */*; q=0.01""",
    "Accept-Encoding" -> """gzip, deflate""",
    "Accept-Language" -> """en-us""",
    "Connection" -> """keep-alive""",
    "Host" -> """localhost:8082""",
    "Referer" -> """http://localhost:8082/bod/reservations/create""",
    "User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10""",
    "X-Requested-With" -> """XMLHttpRequest"""
  )

  val headers_5 = headers_1 ++ Map(
    "Content-Length" -> """146""",
    "Content-Type" -> """application/x-www-form-urlencoded""",
    "Origin" -> """http://localhost:8082""",
    "Pragma" -> """no-cache""",
    "Referer" -> """http://localhost:8082/bod/reservations/create"""
  )

  val headers_6 = headers_1 ++ Map(
    "Origin" -> """http://localhost:8082""",
    "Pragma" -> """no-cache""",
    "Referer" -> """http://localhost:8082/bod/reservations/create"""
  )


  def apply = {
    val scn = scenario("List virtual ports and create a reservation")
      .exec(
        http("dashboard")
        .get("/bod/")
        .headers(headers_1)
      )
      .pause(200, 300, MILLISECONDS)
      .exec(
        http("list reservations")
        .get("/bod/reservations")
        .headers(headers_2)
      )
      .pause(1, 2)
      .exec(
        http("create a reservation")
        .get("/bod/reservations/create")
        .headers(headers_3)
      )
      .pause(0, 100, MILLISECONDS)
      .exec(
        http("get ports in json")
        .get("/bod/teams/17/ports")
        .headers(headers_4)
      )
      .pause(1, 2)
      .exec(
        http("submit the reservation")
        .post("/bod/reservations")
        .param("virtualResourceGroup", "17")
        .param("sourcePort", "104")
        .param("destinationPort", "175")
        .param("startDate", "2012-02-17")
        .param("startTime", "12:00")
        .param("endDate", "2012-02-17")
        .param("endTime", "16:00")
        .param("bandwidth", "162")
        .headers(headers_5)
        .check(status.is(302))
      )
      .pause(0, 100, MILLISECONDS)
      .exec(
        http("list reservation")
        .get("/bod/reservations")
        .headers(headers_6)
      )

    List(scn.configure.protocolConfig(httpConf).users(5).ramp(5 seconds))
  }
}

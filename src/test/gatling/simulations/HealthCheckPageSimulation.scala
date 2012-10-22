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
import bootstrap._
import akka.util.duration._

class HealthCheckPageSimulation extends Simulation {

  def apply = {

    val baseUrl = "http://localhost:8082/bod"
    val httpConf = httpConfig.baseURL(baseUrl)

    val scn = scenario("Health check page")
      .repeat(10) {
        exec(http("Healthcheck page").get("/healthcheck"))
      }

    List(scn.configure.protocolConfig(httpConf).users(1))
  }
}

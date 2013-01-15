/**
 * Copyright (c) 2012, SURFnet BV
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
import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.jdbc.Predef._
import akka.util.duration._

class CreateReservationSimulation extends Simulation {

  val baseUrl = "http://localhost:8082/bod"
  val httpConf = httpConfig.baseURL(baseUrl)

  val oauthServer = "http://localhost:8080"

  val timeFeeder = (0 to 23).flatMap(hour =>
      List(
          Map("startTime" -> (hour+":00"), "endTime" -> (hour+":15")),
          Map("startTime" -> (hour+":20"), "endTime" -> (hour+":35")),
          Map("startTime" -> (hour+":40"), "endTime" -> (hour+":55"))
      )
  ).toIterator

  val date = "2013-03-01"

  val scn = scenario("Create a Reservation")
    .feed(timeFeeder)
    .exec(
      http("Reservation form")
        .get("/reservations/create")
        .check(
          css("output#csrf-token").find.saveAs("csrf-token"),
          xpath("//input[@name='virtualResourceGroup']/@value").find.saveAs("virtualResourceGroup"))
    )
    .pause(0.5 seconds, 2 seconds)
    .exec(
      http("Submit new reservation")
        .post("/reservations")
        .param("name", "Gatling test")
        .param("virtualResourceGroup", "${virtualResourceGroup}")
        .param("sourcePort", "160")
        .param("destinationPort", "157")
        .param("startDate", date)
        .param("startTime", "${startTime}")
        .param("endDate", date)
        .param("endTime", "${endTime}")
        .param("bandwidth", "100")
        .param("protectionType", "PROTECTED")
        .param("csrf-token", "${csrf-token}")
        .check(status.is(200))
    )

  setUp(scn.users(15).ramp(2 seconds).protocolConfig(httpConf))
}
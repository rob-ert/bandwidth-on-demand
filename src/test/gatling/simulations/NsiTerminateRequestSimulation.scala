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
import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import java.util.UUID
import io.gatling.http.request.StringBody

class NsiTerminateRequestSimulation extends Simulation {

  val httpConf = http.baseURL("http://localhost:8082/bod")
  val connectionIdFeeder = jdbcFeeder(
    url = "jdbc:postgresql://localhost/bod",
    username = "bod_user",
    password = "",
    sql = "select connection_id from connection where current_state = 'RESERVED'"
  )

  private val OauthToken = "1f5bb411-71ad-406b-a10d-5889f59bdc22"

  val scn = scenario("Terminate reservations through NSI")
    .feed(connectionIdFeeder)
    .exec(
      http("NSI Reserve request")
        .post("/nsi/v1_sc/provider")
        .body(StringBody(s => terminateRequest(s("connection_id").as[String])))
        .header("Authorization", s => s"bearer $OauthToken")
        .check(status.is(200))
    )

  setUp(scn.inject(ramp(50 users) over (5 seconds))).protocols(httpConf)


  private def terminateRequest(connectionId: String): String =
    <soapenv:Envelope
      xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:type="http://schemas.ogf.org/nsi/2011/10/connection/types"
      xmlns:int="http://schemas.ogf.org/nsi/2011/10/connection/interface">
      <soapenv:Header />
      <soapenv:Body>
        <int:terminateRequest>
          <int:correlationId>{ UUID.randomUUID() }</int:correlationId>
          <int:replyTo>http://localhost:9000/reply</int:replyTo>
          <type:terminate>
            <requesterNSA>urn:ogf:network:nsa:surfnet-nsi-requester</requesterNSA>
            <providerNSA>urn:ogf:network:nsa:surfnet.nl</providerNSA>
            <connectionId>{ connectionId }</connectionId>
          </type:terminate>
        </int:terminateRequest>
      </soapenv:Body>
    </soapenv:Envelope>.toString
}
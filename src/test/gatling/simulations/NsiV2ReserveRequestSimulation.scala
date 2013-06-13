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
import io.gatling.core.scenario.configuration.Simulation
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.util.UUID
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import scala.xml.XML
import scala.xml.Elem
import io.gatling.http.request.builder.AbstractHttpRequestBuilder
import io.gatling.http.request.builder.HttpRequestBaseBuilder
import io.gatling.core.action.builder.ActionBuilder

class NsiV2ReserveRequestSimulation extends Simulation {

  val baseUrl = "http://localhost:8082/bod"
  val httpConf = httpConfig.baseURL(baseUrl)

  val reservationTimeFeeder =
    (0 to 20).flatMap(hours =>
      (0 to 50 by 10) map (minutes =>
        Map("startTime" -> DateTime.now().plusHours(hours).plusMinutes(minutes), "endTime" -> DateTime.now().plusHours(hours).plusMinutes(minutes + 5))
      )
    ).toIterator

  private val EndPoint = "/nsi/v2/provider"
  private val OauthToken = "f44b1e47-0a19-4c11-861b-c9abf82d4cbf"
  private val SourceStpLocalId = "21"
  private val DestinationStpLocalId = "24"
  private val StpNetworkId = "urn:ogf:network:stp:surfnet.nl"

  val scn = scenario("Create a Reservation through NSI")
    .feed(reservationTimeFeeder)
    .exec(http("Reserve")
      .post(EndPoint)
      .body(s => reserveRequest(s.get[DateTime]("startTime"), s.get[DateTime]("endTime")))
      .oAuthHeader(OauthToken)
      .check(
        // xpath checker gave java.nio.BufferUnderflowException exception
        bodyString.transform(_.map(xml => (XML.loadString(xml) \\ "connectionId").text)).saveAs("connectionId"),
        status.is(200)))
    .exec(http("Commmit connection")
      .post(EndPoint)
      .body(s => reserveCommitRequest(s.get("connectionId").get))
      .oAuthHeader(OauthToken)
      .check(status.is(200)))
    .pause(2 seconds)
    .exec(http("Query connection")
      .post(EndPoint)
      .body(s => querySummarySyncRequest(s.get("connectionId").get))
      .oAuthHeader(OauthToken)
      .check(
        bodyString.transform(_.map(xml => (XML.loadString(xml) \\ "reservationState" \ "state").text)).is("Reserved"),
        status.is(200)))
    .exec(http("Provision connection")
      .post(EndPoint)
      .body(s => provisionRequest(s.get("connectionId").get))
      .oAuthHeader(OauthToken)
      .check(status.is(200)))

  setUp(scn.inject(ramp(30 users) over (10 seconds)).protocolConfig(httpConf))

  implicit class NsiHttpRequestBuilder[B <: AbstractHttpRequestBuilder[B]](builder: AbstractHttpRequestBuilder[B]) {
    def oAuthHeader(token: String): B = builder.header("Authorization", session => s"bearer $OauthToken")
  }

  private def reserveCommitRequest(connectionId: String): String = wrap(
    <type:reserveCommit>
      <connectionId>{ connectionId }</connectionId>
    </type:reserveCommit>).toString

  private def querySummarySyncRequest(connectionId: String) = wrap(
    <type:querySummarySync>
      <connectionId>{ connectionId }</connectionId>
    </type:querySummarySync>).toString

  private def provisionRequest(connectionId: String) = wrap(
    <type:provision>
      <connectionId>{ connectionId }</connectionId>
    </type:provision>).toString

  private def reserveRequest(startTime: Option[DateTime], endTime: Option[DateTime]): String = wrap(
    <type:reserve>
      <description>Gatling test</description>
      <criteria version="0">
        <schedule>
          <startTime>{ printDateTime(startTime.get) }</startTime>
          <endTime>{ printDateTime(endTime.get) }</endTime>
        </schedule>
        <bandwidth>1000</bandwidth>
        <serviceAttributes/>
        <path>
          <directionality>Bidirectional</directionality>
          <sourceSTP>
            <networkId>{ StpNetworkId }</networkId>
            <localId>{ SourceStpLocalId }</localId>
          </sourceSTP>
          <destSTP>
            <networkId>{ StpNetworkId }</networkId>
            <localId>{ DestinationStpLocalId }</localId>
          </destSTP>
        </path>
      </criteria>
    </type:reserve>).toString

  private def wrap(body: Elem) =
    <soapenv:Envelope
      xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:head="http://schemas.ogf.org/nsi/2013/04/framework/headers"
      xmlns:type="http://schemas.ogf.org/nsi/2013/04/connection/types">
      <soapenv:Header>
        <head:nsiHeader>
          <protocolVersion>2.0</protocolVersion>
          <correlationId>{ "urn:uuid:" + UUID.randomUUID().toString() }</correlationId>
          <requesterNSA>urn:ogf:network:nsa:surfnet-nsi-requester</requesterNSA>
          <providerNSA>urn:ogf:network:nsa:surfnet.nl</providerNSA>
          <replyTo>http://localhost:9000/reply</replyTo>
        </head:nsiHeader>
      </soapenv:Header>
      <soapenv:Body>
        { body }
      </soapenv:Body>
    </soapenv:Envelope>

  private def printDateTime(dateTime: DateTime) = ISODateTimeFormat.dateTime().print(dateTime)

}

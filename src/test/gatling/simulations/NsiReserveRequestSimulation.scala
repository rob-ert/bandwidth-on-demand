import com.excilys.ebi.gatling.core.scenario.configuration.Simulation
import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.jdbc.Predef._
import akka.util.duration._
import java.util.UUID
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime

class NsiReserveRequestSimulation extends Simulation {

  val baseUrl = "http://localhost:8082/bod"
  val httpConf = httpConfig.baseURL(baseUrl)

  val timeFeeder = (0 to 23).flatMap(hour =>
      List(
          Map("startTime" -> DateTime.now().plusHours(hour).plusMinutes(0), "endTime" -> DateTime.now().plusHours(hour).plusMinutes(5)),
          Map("startTime" -> DateTime.now().plusHours(hour).plusMinutes(10), "endTime" -> DateTime.now().plusHours(hour).plusMinutes(15)),
          Map("startTime" -> DateTime.now().plusHours(hour).plusMinutes(20), "endTime" -> DateTime.now().plusHours(hour).plusMinutes(25)),
          Map("startTime" -> DateTime.now().plusHours(hour).plusMinutes(30), "endTime" -> DateTime.now().plusHours(hour).plusMinutes(35)),
          Map("startTime" -> DateTime.now().plusHours(hour).plusMinutes(40), "endTime" -> DateTime.now().plusHours(hour).plusMinutes(45)),
          Map("startTime" -> DateTime.now().plusHours(hour).plusMinutes(50), "endTime" -> DateTime.now().plusHours(hour).plusMinutes(55))
      )
  ).toIterator

  private val oauthToken = "1f5bb411-71ad-406b-a10d-5889f59bdc22"
  private val sourceStp = "urn:ogf:network:stp:surfnet.nl:19"
  private val destinationStp = "urn:ogf:network:stp:surfnet.nl:22"

  val scn = scenario("Create a Reservation through NSI")
    .feed(timeFeeder)
    .exec(
      http("NSI Reserve request")
        .post("/nsi/v1_sc/provider")
        .body(reserveRequest)
        .header("Authorization" -> ("bearer " + oauthToken))
        .check(status.is(200))
    )

  setUp(scn.users(10).ramp(2 seconds).protocolConfig(httpConf))


  private def reserveRequest(session: Session): String =
    <soapenv:Envelope
      xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:type="http://schemas.ogf.org/nsi/2011/10/connection/types"
      xmlns:int="http://schemas.ogf.org/nsi/2011/10/connection/interface">
      <soapenv:Header />
      <soapenv:Body>
        <int:reserveRequest>
          <int:correlationId>{ UUID.randomUUID() }</int:correlationId>
          <int:replyTo>http://localhost:9000/reply</int:replyTo>
          <type:reserve>
            <requesterNSA>urn:ogf:network:nsa:surfnet-nsi-requester</requesterNSA>
            <providerNSA>urn:ogf:network:nsa:surfnet.nl</providerNSA>
            <reservation>
              <globalReservationId/>
              <description>Gatling NSI Reserve</description>
              <connectionId>{ UUID.randomUUID() }</connectionId>
              <serviceParameters>
                <schedule>
                  <startTime>{ printDateTime(session.getTypedAttribute[DateTime]("startTime")) }</startTime>
                  <endTime>{ printDateTime(session.getTypedAttribute[DateTime]("endTime")) }</endTime>
                </schedule>
                <bandwidth>
                  <desired>100</desired>
                </bandwidth>
              </serviceParameters>
              <path>
                <directionality>Bidirectional</directionality>
                <sourceSTP>
                  <stpId>{ sourceStp }</stpId>
                </sourceSTP>
                <destSTP>
                  <stpId>{ destinationStp }</stpId>
                </destSTP>
              </path>
            </reservation>
          </type:reserve>
        </int:reserveRequest>
      </soapenv:Body>
    </soapenv:Envelope>.toString

  private def printDateTime(dateTime: DateTime) = ISODateTimeFormat.dateTime().print(dateTime)
}
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

	val headers_5 = headers_1 ++ Map(
		"Referer" -> """http://localhost:8082/bod/reservations"""
	)

	val headers_6 = headers_1 ++ Map(
		"Referer" -> """http://localhost:8082/bod/virtualports"""
	)

	val headers_7 = Map(
		"Accept" -> """*/*""",
		"Accept-Encoding" -> """gzip, deflate""",
		"Accept-Language" -> """en-us""",
		"Connection" -> """keep-alive""",
		"Host" -> """localhost:8082""",
		"Referer" -> """http://localhost:8082/bod/virtualports/request""",
		"User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10"""
	)

	val headers_8 = headers_1 ++ Map(
		"Referer" -> """http://localhost:8082/bod/virtualports/request"""
	)

	val headers_9 = headers_1 ++ Map(
		"Content-Length" -> """81""",
		"Content-Type" -> """application/x-www-form-urlencoded""",
		"Origin" -> """http://localhost:8082""",
		"Pragma" -> """no-cache""",
		"Referer" -> """http://localhost:8082/bod/virtualports/request?id=1"""
	)

	val headers_11 = Map(
		"Accept" -> """application/json, text/javascript, */*; q=0.01""",
		"Accept-Encoding" -> """gzip, deflate""",
		"Accept-Language" -> """en-us""",
		"Connection" -> """keep-alive""",
		"Host" -> """localhost:8082""",
		"Referer" -> """http://localhost:8082/bod/reservations/create""",
		"User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10""",
		"X-Requested-With" -> """XMLHttpRequest"""
	)

	val headers_12 = headers_1 ++ Map(
		"Content-Length" -> """146""",
		"Content-Type" -> """application/x-www-form-urlencoded""",
		"Origin" -> """http://localhost:8082""",
		"Pragma" -> """no-cache""",
		"Referer" -> """http://localhost:8082/bod/reservations/create"""
	)


	val headers_13 = headers_1 ++ Map(
		"Origin" -> """http://localhost:8082""",
		"Pragma" -> """no-cache""",
		"Referer" -> """http://localhost:8082/bod/reservations/create"""
	)


  val scn = scenario("List virtual ports and create a reservation")
		.exec(
			http("dashboard")
			.get("/bod/")
			.headers(headers_1)
		)
		.pause(200 milliseconds, 300 milliseconds)
		.exec(
			http("list reservations")
			.get("/bod/reservations")
			.headers(headers_2)
		)
		.pause(2, 3)
		.exec(
			http("list virutal ports")
			.get("/bod/virtualports")
			.headers(headers_5)
		)
		.pause(2, 3)
		.exec(
			http("request virtual port")
			.get("/bod/virtualports/request")
			.headers(headers_6)
		)
		.pause(2, 3)
		.exec(
			http("select institute")
			.get("/bod/virtualports/request")
			.queryParam("id", "1")
			.headers(headers_8)
		)
		.pause(7, 8)
		.exec(
			http("send virutal port request")
			.post("/bod/virtualports/request")
			.param("virtualResourceGroupId", "17")
			.param("message", "I like to have a port")
			.param("physicalResourceGroupId", "1")
			.headers(headers_9)
		)
		.pause(4, 5)
		.exec(
			http("create a reservation")
			.get("/bod/reservations/create")
			.headers(headers_8)
		)
		.pause(0 milliseconds, 100 milliseconds)
		.exec(
			http("get port in javascript")
			.get("/bod/teams/17/ports")
			.headers(headers_11)
		)
		.pause(1, 2)
		.exec(
			http("submit the reservation")
			.post("/bod/reservations")
			.param("virtualResourceGroup", "17")
			.param("sourcePort", "104")
			.param("destinationPort", "175")
			.param("startDate", "2012-02-15")
			.param("startTime", "12:00")
			.param("endDate", "2012-02-15")
			.param("endTime", "16:00")
			.param("bandwidth", "162")
			.headers(headers_12)
			.check(status.is(302))
		)
		.pause(0 milliseconds, 100 milliseconds)
		.exec(
			http("list reservation")
			.get("/bod/reservations")
			.headers(headers_13)
		)

	  setUp(scn.users(50).ramp(5 seconds).protocolConfig(httpConf))
}

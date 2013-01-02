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
package old

import com.excilys.ebi.gatling.core.Predef._

import com.excilys.ebi.gatling.http.Predef._
import akka.util.duration._

class ManagerSimulation extends Simulation {
	val urlBase = "http://localhost:8082"

	val httpConf = httpConfig.baseURL(urlBase).proxy("localhost", 8082)

	val headers_1 = Map(
		"Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
		"Accept-Encoding" -> """gzip, deflate""",
		"Accept-Language" -> """en-us""",
		"Connection" -> """keep-alive""",
		"Host" -> """localhost:8082""",
		"Proxy-Connection" -> """keep-alive""",
		"Referer" -> """http://localhost:8082/bod/manager""",
		"User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10"""
	)


	val headers_2 = Map(
		"Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
		"Accept-Encoding" -> """gzip, deflate""",
		"Accept-Language" -> """en-us""",
		"Connection" -> """keep-alive""",
		"Host" -> """localhost:8082""",
		"Proxy-Connection" -> """keep-alive""",
		"Referer" -> """http://localhost:8082/bod/manager/teams""",
		"User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10"""
	)


	val headers_3 = Map(
		"Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
		"Accept-Encoding" -> """gzip, deflate""",
		"Accept-Language" -> """en-us""",
		"Connection" -> """keep-alive""",
		"Host" -> """localhost:8082""",
		"Proxy-Connection" -> """keep-alive""",
		"Referer" -> """http://localhost:8082/bod/manager/physicalresourcegroups""",
		"User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10"""
	)


	val headers_4 = Map(
		"Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
		"Accept-Encoding" -> """gzip, deflate""",
		"Accept-Language" -> """en-us""",
		"Connection" -> """keep-alive""",
		"Host" -> """localhost:8082""",
		"Proxy-Connection" -> """keep-alive""",
		"Referer" -> """http://localhost:8082/bod/manager/virtualports""",
		"User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10"""
	)


	val headers_5 = Map(
		"Accept" -> """application/json, text/javascript, */*; q=0.01""",
		"Accept-Encoding" -> """gzip, deflate""",
		"Accept-Language" -> """en-us""",
		"Connection" -> """keep-alive""",
		"Host" -> """localhost:8082""",
		"Proxy-Connection" -> """keep-alive""",
		"Referer" -> """http://localhost:8082/bod/manager/physicalports""",
		"User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10""",
		"X-Requested-With" -> """XMLHttpRequest"""
	)


	val headers_8 = Map(
		"Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
		"Accept-Encoding" -> """gzip, deflate""",
		"Accept-Language" -> """en-us""",
		"Connection" -> """keep-alive""",
		"Host" -> """localhost:8082""",
		"Proxy-Connection" -> """keep-alive""",
		"Referer" -> """http://localhost:8082/bod/manager/physicalports""",
		"User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10"""
	)


	val headers_10 = Map(
		"Accept" -> """*/*""",
		"Accept-Encoding" -> """gzip, deflate""",
		"Accept-Language" -> """en-us""",
		"Connection" -> """keep-alive""",
		"Host" -> """localhost:8082""",
		"Proxy-Connection" -> """keep-alive""",
		"Referer" -> """http://localhost:8082/bod/manager/teams?id=12""",
		"User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10"""
	)


	val headers_12 = Map(
		"Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
		"Accept-Encoding" -> """gzip, deflate""",
		"Accept-Language" -> """en-us""",
		"Connection" -> """keep-alive""",
		"Host" -> """localhost:8082""",
		"Proxy-Connection" -> """keep-alive""",
		"Referer" -> """http://localhost:8082/bod/manager/teams?id=12""",
		"User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10"""
	)


	val headers_14 = Map(
		"Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
		"Accept-Encoding" -> """gzip, deflate""",
		"Accept-Language" -> """en-us""",
		"Connection" -> """keep-alive""",
		"Host" -> """localhost:8082""",
		"Proxy-Connection" -> """keep-alive""",
		"Referer" -> """http://localhost:8082/bod/manager/physicalresourcegroups/edit?id=1""",
		"User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10"""
	)


	val headers_16 = Map(
		"Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
		"Accept-Encoding" -> """gzip, deflate""",
		"Accept-Language" -> """en-us""",
		"Connection" -> """keep-alive""",
		"Host" -> """localhost:8082""",
		"Proxy-Connection" -> """keep-alive""",
		"Referer" -> """http://localhost:8082/bod/manager/virtualports?id=13""",
		"User-Agent" -> """Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.53.11 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10"""
	)


	val scn = scenario("Manager clicking around")
		.exec(
		http("Get virutal resource groups")
		.get("/bod/manager/teams")
		.headers(headers_1)
	)
		.pause(2, 3)
		.exec(
		http("Get physical resource groups")
		.get("/bod/manager/physicalresourcegroups")
		.headers(headers_2)
	)
		.pause(1, 2)
		.exec(
		http("Get virtual ports")
		.get("/bod/manager/virtualports")
		.headers(headers_3)
	)
		.pause(1, 2)
		.exec(
		http("Get physical ports")
		.get("/bod/manager/physicalports")
		.headers(headers_4)
	)
		.pause(1, 2)
		.exec(
		http("Show virutal port (JSON)")
		.get("/bod/manager/physicalports/5/virtualports")
		.headers(headers_5)
	)
	.pause(0 milliseconds, 100 milliseconds)
		.exec(
		http("Show virutal port (JSON)")
		.get("/bod/manager/physicalports/6/virtualports")
		.headers(headers_5)
	)
		.pause(1, 2)
		.exec(
		http("Show virutal port (JSON)")
		.get("/bod/manager/physicalports/7/virtualports")
		.headers(headers_5)
	)
		.pause(2, 3)
		.exec(
		http("List virtual resource groups")
		.get("/bod/manager/teams")
		.headers(headers_8)
	)
		.pause(1, 2)
		.exec(
		http("Show virtual resource groups")
		.get("/bod/manager/teams")
		.queryParam("id", "12")
		.headers(headers_2)
	)
		.pause(0 milliseconds, 100 milliseconds)
		.exec(
		http("request_10")
		.get("/bod/resources/images/list.png")
		.headers(headers_10)
	)
		.pause(0 milliseconds, 100 milliseconds)
		.exec(
		http("request_11")
		.get("/bod/resources/images/create.png")
		.headers(headers_10)
	)
		.pause(1, 2)
		.exec(
		http("request_12")
		.get("/bod/manager/physicalresourcegroups")
		.headers(headers_12)
	)
		.pause(1, 2)
		.exec(
		http("request_13")
		.get("/bod/manager/physicalresourcegroups/edit")
		.queryParam("id", "1")
		.headers(headers_3)
	)
		.pause(1, 2)
		.exec(
		http("request_14")
		.get("/bod/manager/virtualports")
		.headers(headers_14)
	)
		.pause(1, 2)
		.exec(
		http("request_15")
		.get("/bod/manager/virtualports")
		.queryParam("id", "13")
		.headers(headers_4)
	)
		.pause(2, 3)
		.exec(
		http("request_16")
		.get("/bod/manager/virtualports/edit")
		.queryParam("id", "13")
		.headers(headers_16)
	)

	setUp(scn.users(1000).ramp(10).protocolConfig(httpConf))
}
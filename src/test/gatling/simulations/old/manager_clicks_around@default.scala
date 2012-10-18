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


	def apply = {
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
			.pause(900, 1000, MILLISECONDS)
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
			.pause(0, 100, MILLISECONDS)
			.exec(
			http("request_10")
			.get("/bod/resources/images/list.png")
			.headers(headers_10)
		)
			.pause(0, 100, MILLISECONDS)
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

  	List(
  		scn.configure.users(1000).ramp(10)protocolConfig(httpConf)
  	)
	}
}
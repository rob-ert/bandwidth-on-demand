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
/**
 *  usage: groovy unsub.groovy [url] [start] [end]
 *  start and end are the subscription id's, url is the url of the subscriptionservice
 *  Example: "groovy unsub.groovy http://145.145.64.78:9006/mtosi/fmw/NotificationProducer 20 30" will unsub from topic and service with id's 20 until 30 (inclusive).
 *  Note: first execution may be slow because groovy will be downloading the 'http-builder' dependency mentioned in the @Grab annotation below
 *  Requires: Groovy 1.6+
 *
 */
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.6')
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*


def url = args[0] as String
def start = args[1] as Integer
def end = args[2] as Integer
def http = new HTTPBuilder(url)
def topics = ["service", "fault"]
for (i in start..end) {
	
	topics.each { topic ->

        def message = """<?xml version='1.0' encoding='UTF-8'?>
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
<S:Header><header
xmlns="http://www.tmforum.org/mtop/fmw/xsd/hdr/v1"
xmlns:ns2="http://www.tmforum.org/mtop/fmw/xsd/msg/v1"
xmlns:ns3="http://www.tmforum.org/mtop/fmw/xsd/notmsg/v1"
xmlns:ns4="http://www.tmforum.org/mtop/fmw/xsd/gen/v1"
xmlns:ns5="http://www.tmforum.org/mtop/mri/xsd/tpr/v1"
xmlns:ns6="http://www.tmforum.org/mtop/fmw/xsd/cei/v1"
xmlns:ns7="http://www.tmforum.org/mtop/fmw/xsd/ei/v1"
xmlns:ns8="http://www.tmforum.org/mtop/fmw/xsd/nam/v1"
xmlns:ns9="http://www.tmforum.org/mtop/nra/xsd/alm/v1"
xmlns:ns10="http://www.tmforum.org/mtop/nra/xsd/prc/v1"
xmlns:ns11="http://www.tmforum.org/mtop/nra/xsd/com/v1"
xmlns:ns12="http://www.tmforum.org/mtop/fmw/xsd/cornot/v1">
    <activityName>unsubscribe</activityName>
    <msgName>unsubscribeRequest</msgName>
    <msgType>REQUEST</msgType>
    <senderURI>http://localhost:9009</senderURI>
    <destinationURI>${url}</destinationURI>
    <communicationPattern>SimpleResponse</communicationPattern>
    <communicationStyle>RPC</communicationStyle>
    <timestamp>${new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date())}</timestamp>
    </header>
    </S:Header>
    <S:Body>
    <ns3:unsubscribeRequest
xmlns="http://www.tmforum.org/mtop/fmw/xsd/hdr/v1"
xmlns:ns2="http://www.tmforum.org/mtop/fmw/xsd/msg/v1"
xmlns:ns3="http://www.tmforum.org/mtop/fmw/xsd/notmsg/v1"
xmlns:ns4="http://www.tmforum.org/mtop/fmw/xsd/gen/v1"
xmlns:ns5="http://www.tmforum.org/mtop/mri/xsd/tpr/v1"
xmlns:ns6="http://www.tmforum.org/mtop/fmw/xsd/cei/v1"
xmlns:ns7="http://www.tmforum.org/mtop/fmw/xsd/ei/v1"
xmlns:ns8="http://www.tmforum.org/mtop/fmw/xsd/nam/v1"
xmlns:ns9="http://www.tmforum.org/mtop/nra/xsd/alm/v1"
xmlns:ns10="http://www.tmforum.org/mtop/nra/xsd/prc/v1"
xmlns:ns11="http://www.tmforum.org/mtop/nra/xsd/com/v1"
xmlns:ns12="http://www.tmforum.org/mtop/fmw/xsd/cornot/v1">
<ns3:subscriptionID>${i}</ns3:subscriptionID><ns3:topic>${topic}</ns3:topic></ns3:unsubscribeRequest></S:Body></S:Envelope>
"""


        http.request(POST, XML) { req ->
            requestContentType = "text/xml"
            body = message
            response.success = { resp, xml ->
			    println "${resp.statusLine} Ok, unsubbed... id: ${i},topic: ${topic}"
			  }
			  response.failure = { resp, xml ->
			    println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}, xml: ${xml}"
			  }
        }
    }

	
}




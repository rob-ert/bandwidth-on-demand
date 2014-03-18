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
package nl.surfnet.bod.web.security;

import static com.google.common.base.Strings.nullToEmpty;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import java.util.Optional;

import nl.surfnet.bod.domain.oauth.VerifiedToken;
import nl.surfnet.bod.service.OAuthServerService;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.ShibbolethConstants;

public class RequestHeaderAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

  private final Logger logger = LoggerFactory.getLogger(RequestHeaderAuthenticationFilter.class);

  private final Function<HttpServletRequest, String> imitateNameId = new Function<HttpServletRequest, String>() {
    @Override
    public String apply(HttpServletRequest request) {
      String user = nullToEmpty(request.getParameter("nameId"));
      return user.isEmpty() ? env.getImitateShibbolethUserId() : user;
    }
  };

  private final Function<HttpServletRequest, String> imitateDisplayName = new Function<HttpServletRequest, String>() {
    @Override
    public String apply(HttpServletRequest request) {
      String name = nullToEmpty(request.getParameter("displayName"));
      return name.isEmpty() ? env.getImitateShibbolethDisplayName() : name;
    }
  };

  private final Function<HttpServletRequest, String> imitateEmail = new Function<HttpServletRequest, String>() {
    @Override
    public String apply(HttpServletRequest request) {
      String email = nullToEmpty(request.getParameter("email"));
      return email.isEmpty() ? env.getImitateShibbolethEmail() : email;
    }
  };

  @Resource(name = "bodEnvironment")
  private Environment env;

  @Resource
  private OAuthServerService oAuthServerService;

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
    if (isNsiV1Request(request)) {
      return getPrincipalFromOauth2Header(request);
    } else if(isNsiV2Request(request)) {
      return getPrincipalFromSoapHeader(request);
    }
    return getPrincipalFromHeaders(request);
  }

  @Override
  /**
   * Copy the request input stream so that we may read it more than once
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
    super.doFilter(new CustomHttpServletRequestWrapper((HttpServletRequest) request), response, chain);
  }

  private Object getPrincipalFromSoapHeader(HttpServletRequest request) {
    final List<String> tokensFromSoapHeader = getTokensFromSoapHeader(request);
    for (String candidate: tokensFromSoapHeader) {
      final RichPrincipal richPrincipal = getRichPrincipalByToken(candidate);
      if (richPrincipal != null) {
        return richPrincipal;
      }
    }
    return null; // no token validated, so returning a null Principal
  }

  private List<String> getTokensFromSoapHeader(final HttpServletRequest request) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(request.getInputStream());
      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();

      final String query = "//*[local-name()='nsiHeader']/*[local-name()='sessionSecurityAttr']/*[local-name()='Attribute'][@Name='token']/*[local-name()='AttributeValue']/text()";
      XPathExpression expr = xpath.compile(query);
      NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
      final ArrayList<String> result = new ArrayList<>();
      for (int i = 0; i < nodes.getLength(); i++) {
        Node currentItem = nodes.item(i);
        result.add(currentItem.getTextContent().trim());
      }
      return result;
    } catch (Exception e) {
      throw new IllegalArgumentException("Unable to find authorization tokens in soap headers", e);
    }
  }

  private boolean isNsiV2Request(HttpServletRequest request) {
    return request.getRequestURI().contains("/nsi/v2");
  }

  private boolean isNsiV1Request(HttpServletRequest request) {
    return request.getRequestURI().contains("/nsi/v1_sc");
  }

  private Object getPrincipalFromHeaders(HttpServletRequest request) {
    String nameId = getRequestHeaderOrImmitate(request, ShibbolethConstants.NAME_ID, imitateNameId);
    String displayName = getRequestHeaderOrImmitate(request, ShibbolethConstants.DISPLAY_NAME, imitateDisplayName);
    String email = getRequestHeaderOrImmitate(request, ShibbolethConstants.EMAIL, imitateEmail);

    logger.info("Found Shibboleth name-id: '{}', displayName: '{}', email: {}", new Object[] {nameId, displayName, email});

    if (nameId.isEmpty() || displayName.isEmpty()) {
      return null;
    }

    return new RichPrincipal(nameId, displayName, email);
  }

  private RichPrincipal getPrincipalFromOauth2Header(HttpServletRequest request) {
    String authorizationHeader = request.getHeader("Authorization");

    if (authorizationHeader == null || !authorizationHeader.startsWith("bearer ")) {
      logger.warn("Could not find a OAuth2 authorization header");
      return null;
    }

    String accessToken = authorizationHeader.split(" ")[1];

    return getRichPrincipalByToken(accessToken);
  }

  private RichPrincipal getRichPrincipalByToken(String accessToken) {
    Optional<VerifiedToken> verifiedToken = oAuthServerService.getVerifiedToken(accessToken);
    logger.debug("Found verifiedToken {}", verifiedToken);

    return verifiedToken.map(token -> new RichPrincipal(
      token.getPrincipal().getName(),
      token.getPrincipal().getAttributes().get("displayName"),
      token.getPrincipal().getAttributes().get("email"),
      token.getScopes())).orElse(null);
  }

  private String getRequestHeaderOrImmitate(
          HttpServletRequest request, String header, Function<HttpServletRequest, String> immitateValue) {
    String value = nullToEmpty(request.getHeader(header));

    String headerValue = value.isEmpty() && env.getImitateShibboleth() ? immitateValue.apply(request) : value;

    return new String(headerValue.getBytes(Charsets.ISO_8859_1), Charsets.UTF_8);
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }

  protected void setEnvironment(Environment environment) {
    this.env = environment;
  }

  /**
   * Keeps a copy of the request body
   */
  private static class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private String body;

    public CustomHttpServletRequestWrapper(HttpServletRequest request) {
      super(request);

      try {
        body = IOUtils.toString(request.getInputStream());
      } catch (IOException exception) {
        throw new RuntimeException("Error reading the request body", exception);
      }
    }

    @Override
    public ServletInputStream getInputStream () throws IOException {
      final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
      ServletInputStream inputStream = new ServletInputStream() {
        public int read () throws IOException {
          return byteArrayInputStream.read();
        }
      };
      return inputStream;
    }
  }
}

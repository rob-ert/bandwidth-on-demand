package nl.surfnet.bod.sabng;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

public class SabNgEntitlementsHandler {

  private static final String REQUEST_TEMPLATE_LOCATION = "/xmlsabng/request-entitlement-template.xml";

  public String createRequest(String issuer, String nameId) {
    String template;
    try {
      template = IOUtils.toString(this.getClass().getResourceAsStream(REQUEST_TEMPLATE_LOCATION), Charsets.UTF_8);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    return MessageFormat.format(template, issuer, nameId);
  }

  public List<String> retrieveEntitlements(InputStream responseStream) {
    List<String> entitlements = Lists.newArrayList();

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // TODO CHECK factory.setNamespaceAware(true);

    Document document;
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      document = builder.parse(responseStream);
    }
    catch (ParserConfigurationException | SAXException | IOException e) {
      throw new RuntimeException(e);
    }

    javax.xml.xpath.XPathFactory xPathFactory = javax.xml.xpath.XPathFactory.newInstance();
    XPath xPath = xPathFactory.newXPath();

    xPath.setNamespaceContext(new SabNgNamespaceResolver());

    XPathExpression expression;
    Object result;
    try {
      expression = xPath.compile("//SOAP-ENV:Envelope/SOAP-ENV:Body");
      result = expression.evaluate(document, XPathConstants.NODESET);
    }
    catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }

    NodeList nodes = (NodeList) result;
    for (int i = 0; i < nodes.getLength(); i++) {
      System.err.println(nodes.item(i));
    }

    return entitlements;
  }

  private class SabNgNamespaceResolver implements NamespaceContext {

    @Override
    public String getNamespaceURI(String prefix) {

      switch (prefix) {
      case "SOAP-ENV":
        return "http://schemas.xmlsoap.org/soap/envelope/";
      case "samlp":
        return "urn:oasis:names:tc:SAML:2.0:protocol";
      case "saml":
        return "urn:oasis:names:tc:SAML:2.0:assertion";
      case "xsi":
        return "http://www.w3.org/2001/XMLSchema-instance";
      case "xs":
        return "http://www.w3.org/2001/XMLSchema";
      default:
        return XMLConstants.NULL_NS_URI;
      }

    }

    @Override
    public String getPrefix(String namespaceURI) {
      return null;
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
      return null;
    }

  }
}

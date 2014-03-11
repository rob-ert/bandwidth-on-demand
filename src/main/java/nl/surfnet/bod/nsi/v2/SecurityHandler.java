package nl.surfnet.bod.nsi.v2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.ogf.schemas.nsi._2013._12.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._12.framework.headers.SessionSecurityAttrType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.oauth.VerifiedToken;
import nl.surfnet.bod.service.OAuthServerService;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

@Component
/**
 * Reads the SOAP headers, looks for a valid token in the 'securityAttr' element and puts the RichPrincipal in
 * the SecurityContext after validating it.
 *
 * If none is found, halts processing
 */
public class SecurityHandler implements SOAPHandler<SOAPMessageContext> {

  private final Logger LOG = LoggerFactory.getLogger(SecurityHandler.class);

  private static Set<QName> HEADERS = new HashSet<>();

  @Resource
  private OAuthServerService oAuthServerService;

  public SecurityHandler() {
    HEADERS.add(new QName("http://schemas.ogf.org/nsi/2013/12/framework/headers"));
  }

  @Override
  public Set<QName> getHeaders() {
    return HEADERS;
  }

  @Override
  public boolean handleMessage(SOAPMessageContext context) {
    Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    if (outbound) {
      return true; // we are only concerned with incoming messages
    }
    try {
      CommonHeaderType headers = Converters.parseNsiHeader(context.getMessage());
      if (headers.getSessionSecurityAttr().isEmpty()){
        return false;
      }
      // extract all the candidate tokens, until the first one that is valid is found
      for (SessionSecurityAttrType sessionSecurityAttrType: headers.getSessionSecurityAttr()) {
        if (sessionSecurityAttrType.getName().equals("token")) {
          final List<Object> attributeOrEncryptedAttribute = sessionSecurityAttrType.getAttributeOrEncryptedAttribute();
          for (Object attribute: sessionSecurityAttrType.getAttributeOrEncryptedAttribute()) {
            AttributeType attributeType = (AttributeType) attribute;
            String token = (String) attributeType.getAttributeValue().get(0);
            final Optional<VerifiedToken> verifiedToken = oAuthServerService.getVerifiedToken(token);
            if (verifiedToken.isPresent()) {
              // TODO hans handle SecurityContext (?)
              return true;
            }
          }
        }
      }
      return false; // nothing valid was found
    } catch (SOAPException | JAXBException e) {
      throw new RuntimeException("Unable to convert headers", e);
    }

  }

  @Override
  public boolean handleFault(SOAPMessageContext context) {
    return true;
  }

  @Override
  public void close(MessageContext context) {

  }
}

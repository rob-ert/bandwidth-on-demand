package nl.surfnet.bod.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ogf.schemas.nsi._2011._10.connection.types.DirectionalityType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;

public class JaxbUserTypeTest {

    private static final String PATH_TYPE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<pathType xmlns:ns2=\"urn:oasis:names:tc:SAML:2.0:assertion\" xmlns:ns4=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:ns3=\"http://www.w3.org/2001/04/xmlenc#\" xmlns:ns5=\"http://schemas.ogf.org/nsi/2011/10/connection/types\">"
            + "<directionality>Bidirectional</directionality><destSTP><stpId>stp-id</stpId></destSTP></pathType>";
    private JaxbUserType<PathType> subject = new JaxbUserType<>(PathType.class);

    @Test
    public void shouldDeserializeFromXmlString() {
        PathType result = subject.fromXmlString(PATH_TYPE_XML);

        assertNotNull(result);
        assertThat(result.getDirectionality(), is(DirectionalityType.BIDIRECTIONAL));
        assertThat(result.getDestSTP().getStpId(), is("stp-id"));
    }

    @Test
    public void shouldSerializeToXmlString() {
        PathType path = new PathType();
        path.setDirectionality(DirectionalityType.BIDIRECTIONAL);
        ServiceTerminationPointType serviceTerminationPointType = new ServiceTerminationPointType();
        serviceTerminationPointType.setStpId("stp-id");
        path.setDestSTP(serviceTerminationPointType);

        String string = subject.toXmlString(path);

        assertThat(string, is(PATH_TYPE_XML));
    }
}

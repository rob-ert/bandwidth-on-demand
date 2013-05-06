package nl.surfnet.bod.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

public class JaxbUserType<T> implements UserType {

    private static final int[] SQL_TYPES = { Types.LONGVARCHAR };

    private static JAXBContext jaxbContext;
    private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    static {
        try {
            jaxbContext = JAXBContext.newInstance("org.ogf.schemas.nsi._2011._10.connection.types");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final Class<T> type;

    protected JaxbUserType(Class<T> type) {
        this.type = type;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class returnedClass() {
        return type;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return Objects.hashCode(x);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        String string = rs.getString(names[0]);
        if (rs.wasNull()) {
            return null;
        }

        return fromXmlString(string);
    }

    T fromXmlString(String string) {
        try (InputStream input = IOUtils.toInputStream(string, "UTF-8")) {
            XMLStreamReader reader = inputFactory.createXMLStreamReader(input);
            try {
                return jaxbContext.createUnmarshaller().unmarshal(reader, type).getValue();
            } finally {
                reader.close();
            }
        } catch (JAXBException | IOException | XMLStreamException e) {
            throw new HibernateException(e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        String string = toXmlString(value);
        st.setString(index, string);
    }

    String toXmlString(Object value) {
        try (StringWriter writer = new StringWriter()) {
            jaxbContext.createMarshaller().marshal(new JAXBElement<T>(new QName("pathType"), type, type.cast(value)), writer);
            return writer.toString();
        } catch (JAXBException | IOException e) {
            throw new HibernateException(e);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return fromXmlString(toXmlString(value));
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return toXmlString(value);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}

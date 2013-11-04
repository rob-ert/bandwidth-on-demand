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
package nl.surfnet.bod.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JaxbUserType<T> implements UserType {

  private static final int[] SQL_TYPES = { Types.VARCHAR };

  private final JAXBContext jaxbContext;
  private final QName xmlRootElementName;
  private final Class<T> type;

  protected JaxbUserType(JAXBContext jaxbContext, QName xmlRootElementName, Class<T> type) {
    this.jaxbContext = jaxbContext;
    this.xmlRootElementName = xmlRootElementName;
    this.type = type;
  }

  protected JaxbUserType(JAXBContext jaxbContext, JAXBElement<T> nullJaxbElement) {
    this(jaxbContext, nullJaxbElement.getName(), nullJaxbElement.getDeclaredType());
  }

  public QName getXmlRootElementName() {
    return xmlRootElementName;
  }

  public Class<T> getType() {
    return type;
  }

  public JAXBContext getJaxbContext() {
    return jaxbContext;
  }

  public T fromXmlString(String string) {
    if (string == null) {
      return null;
    }
    try (InputStream input = IOUtils.toInputStream(string, "UTF-8")) {
      return jaxbContext.createUnmarshaller().unmarshal(new StreamSource(input), type).getValue();
    } catch (JAXBException | IOException e) {
      throw new IllegalArgumentException("failed to deserialize: " + string, e);
    }
  }

  public String toXmlString(T value) {
    if (value == null) {
      return null;
    }
    try (StringWriter writer = new StringWriter()) {
      jaxbContext.createMarshaller().marshal(new JAXBElement<>(xmlRootElementName, type, value), writer);
      return writer.toString();
    } catch (JAXBException | IOException e) {
      throw new IllegalArgumentException("failed to serialize: " + value, e);
    }
  }

  public T fromDomElement(Element element) {
    try {
      return jaxbContext.createUnmarshaller().unmarshal(element, type).getValue();
    } catch (JAXBException e) {
      throw new IllegalArgumentException("failed to deserialize: " + element, e);
    }
  }

  public Element toDomElement(T value) {
    try {
      JAXBElement<T> jaxb = new JAXBElement<>(xmlRootElementName, type, value);
      DOMResult result = new DOMResult();
      jaxbContext.createMarshaller().marshal(jaxb, result);
      return ((Document) result.getNode()).getDocumentElement();
    } catch (JAXBException e) {
      throw new IllegalArgumentException("failed to serialize: " + value, e);
    }
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
    if (x == y) {
      return true;
    }
    if (!type.isInstance(x) || !type.isInstance(y)) {
      return false;
    }
    Element left = toDomElement(type.cast(x));
    Element right = toDomElement(type.cast(y));
    return left.isEqualNode(right);
  }

  @Override
  public int hashCode(Object x) throws HibernateException {
    return x == null ? 0 : toXmlString(type.cast(x)).hashCode();
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
      throws HibernateException, SQLException {
    String string = rs.getString(names[0]);
    if (rs.wasNull()) {
      return null;
    }

    return fromXmlString(string);
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
      throws HibernateException, SQLException {
    String string = toXmlString(type.cast(value));
    st.setString(index, string);
  }

  @Override
  public Object deepCopy(Object value) throws HibernateException {
    return fromDomElement(toDomElement(type.cast(value)));
  }

  @Override
  public boolean isMutable() {
    return true;
  }

  @Override
  public Serializable disassemble(Object value) throws HibernateException {
    return toXmlString(type.cast(value));
  }

  @Override
  public Object assemble(Serializable cached, Object owner) throws HibernateException {
    return fromXmlString((String) cached);
  }

  @Override
  public Object replace(Object original, Object target, Object owner) throws HibernateException {
    return deepCopy(original);
  }
}

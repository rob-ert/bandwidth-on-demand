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
package org.tmforum.mtop.fmw.xsd.cei.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import org.tmforum.mtop.fmw.xsd.ei.v1.EventInformationType;
import org.tmforum.mtop.fmw.xsd.gen.v1.AnyListType;

/**
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;p xmlns:gen="http://www.tmforum.org/mtop/fmw/xsd/gen/v1" xmlns:tns="http://www.tmforum.org/mtop/fmw/xsd/cei/v1" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;The type definition of the common set of attributes used for all events.&lt;/p&gt;
 * </pre>
 * 
 * 
 * <p>
 * Java class for CommonEventInformationType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CommonEventInformationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="notificationId" type="{http://www.tmforum.org/mtop/fmw/xsd/gen/v1}NotificationIdentifierType" minOccurs="0"/>
 *         &lt;element name="sourceTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="vendorExtensions" type="{http://www.tmforum.org/mtop/fmw/xsd/gen/v1}AnyListType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CommonEventInformationType", propOrder = { "notificationId", "sourceTime", "vendorExtensions" })
@XmlSeeAlso({ EventInformationType.class })
public class CommonEventInformationType {

  protected String notificationId;
  @XmlSchemaType(name = "dateTime")
  protected XMLGregorianCalendar sourceTime;
  protected AnyListType vendorExtensions;

  /**
   * Gets the value of the notificationId property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getNotificationId() {
    return notificationId;
  }

  /**
   * Sets the value of the notificationId property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setNotificationId(String value) {
    this.notificationId = value;
  }

  /**
   * Gets the value of the sourceTime property.
   * 
   * @return possible object is {@link XMLGregorianCalendar }
   * 
   */
  public XMLGregorianCalendar getSourceTime() {
    return sourceTime;
  }

  /**
   * Sets the value of the sourceTime property.
   * 
   * @param value
   *          allowed object is {@link XMLGregorianCalendar }
   * 
   */
  public void setSourceTime(XMLGregorianCalendar value) {
    this.sourceTime = value;
  }

  /**
   * Gets the value of the vendorExtensions property.
   * 
   * @return possible object is {@link AnyListType }
   * 
   */
  public AnyListType getVendorExtensions() {
    return vendorExtensions;
  }

  /**
   * Sets the value of the vendorExtensions property.
   * 
   * @param value
   *          allowed object is {@link AnyListType }
   * 
   */
  public void setVendorExtensions(AnyListType value) {
    this.vendorExtensions = value;
  }

}

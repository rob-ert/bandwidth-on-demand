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
package nl.surfnet.bod.nbi.mtosi;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import nl.surfnet.bod.domain.ReservationStatus;

import org.springframework.util.StringUtils;
import org.tmforum.mtop.fmw.xsd.msg.v1.BaseExceptionMessageType;
import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ActivateException;
import org.tmforum.mtop.sa.xsd.saiexcpt.v1.BasicFailureEventType;
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceAccessPointType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceCharacteristicValueType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceStateType;

public final class MtosiUtils {

  private static final String PTP_FORMAT = "/rack=%s/shelf=%s/slot=%s/port=%s";
  private static final String PTP_WITH_SUB_SLOT_FORMAT = "/rack=%s/shelf=%s/slot=%s/sub_slot=%s/port=%s";

  private MtosiUtils() {
  }

  public static String composeNmsPortId(String managedElement, String ptp) {
    checkArgument(!managedElement.contains("@"));
    checkArgument(!ptp.contains("@"));

    return managedElement + "@" + convertToShortPtP(ptp);
  }

  public static String extractPtpFromNmsPortId(String nmsPortId) {
    checkNotNull(nmsPortId);

    String[] ids = nmsPortId.split("@");
    if (ids.length != 2) {
      throw new IllegalArgumentException("NMS Port Id does not confirm expected format");
    }

    return convertToLongPtP(ids[1]);
  }

  public static BaseExceptionMessageType getBaseExceptionMessage(ActivateException exception) {
    return getBaseExceptionMessage(exception.getFaultInfo().getBasicFailureEvent());
  }

  private static BaseExceptionMessageType getBaseExceptionMessage(BasicFailureEventType basicFailure) {
    return basicFailure.getNotInValidState();
  }

  public static String getSapName(ServiceAccessPointType sap) {
    return findRdnValue("SAP", sap.getName().getValue()).get();
  }

  public static String getRfsName(ResourceFacingServiceType rfs) {
    return findRdnValue("RFS", rfs.getName().getValue()).get();
  }

  public static Optional<String> findRdnValue(final String type, NamingAttributeType nat) {
    return Iterables.tryFind(nat.getRdn(), new Predicate<RelativeDistinguishNameType>() {
      @Override
      public boolean apply(RelativeDistinguishNameType rdn) {
        return rdn.getType().equals(type);
      }
    }).transform(new Function<RelativeDistinguishNameType, String>() {
      @Override
      public String apply(RelativeDistinguishNameType rdn) {
        return rdn.getValue();
      }
    });
  }

  public static Optional<String> findSscValue(final String sscValue,
      List<ServiceCharacteristicValueType> characteristics) {
    return Iterables.tryFind(characteristics, new Predicate<ServiceCharacteristicValueType>() {
      @Override
      public boolean apply(ServiceCharacteristicValueType scv) {
        return findRdnValue("SSC", scv.getSscRef()).get().equals(sscValue);
      }
    }).transform(new Function<ServiceCharacteristicValueType, String>() {
      @Override
      public String apply(ServiceCharacteristicValueType ssc) {
        return ssc.getValue();
      }
    });
  }

  public static void printDescribedByList(List<ServiceCharacteristicValueType> describedByList) {
    Iterator<ServiceCharacteristicValueType> iterator = describedByList.iterator();
    while (iterator.hasNext()) {
      ServiceCharacteristicValueType type = iterator.next();
      System.err
          .println(String.format("Type: %s, SscRef %s", getRdnString(type.getSscRef().getRdn()), type.getValue()));
    }
  }

  private static String getRdnString(List<RelativeDistinguishNameType> rdn) {
    List<String> contents = new ArrayList<>();
    Iterator<RelativeDistinguishNameType> iterator = rdn.iterator();
    while (iterator.hasNext()) {
      RelativeDistinguishNameType item = iterator.next();
      contents.add(item.getType() + " " + item.getValue());
    }
    return StringUtils.collectionToCommaDelimitedString(contents);
  }

  public static String convertToShortPtP(String ptp) {
    return ptp.replace("rack=", "").replace("shelf=", "").replace("sub_slot=", "").replace("slot=", "")
        .replace("port=", "").replaceFirst("/", "").replaceAll("/", "-");
  }

  public static String convertToLongPtP(String shortPtP) {
    Object[] parts = shortPtP.split("-");
    if (parts.length == 4) {
      return String.format(PTP_FORMAT, parts);
    } else if (parts.length == 5) {
      return String.format(PTP_WITH_SUB_SLOT_FORMAT, parts);
    } else {
      throw new IllegalArgumentException("The nmsPortId can not be converted to a ptp");
    }
  }

  public static RelativeDistinguishNameType createRdn(String type, String value) {
    RelativeDistinguishNameType rel = new RelativeDistinguishNameType();
    rel.setType(type);
    rel.setValue(value);
    return rel;
  }

  public static NamingAttributeType createRfs(String name) {
    return createNamingAttrib("RFS", name);
  }

  public static NamingAttributeType createNamingAttrib(String type, String value) {
    NamingAttributeType namingAttributeType = new NamingAttributeType();

    namingAttributeType.getRdn().add(createRdn(type, value));

    return namingAttributeType;
  }

  public static JAXBElement<NamingAttributeType> createNamingAttributeType(String type, String value) {
    return new org.tmforum.mtop.fmw.xsd.coi.v1.ObjectFactory().createCommonObjectInfoTypeName(createNamingAttrib(type,
        value));
  }

  public static ServiceCharacteristicValueType createSscValue(String name, String value) {
    return createSscValue(createNamingAttrib("SSC", name), value);
  }

  private static ServiceCharacteristicValueType createSscValue(NamingAttributeType namingAttributeType, String value) {
    ServiceCharacteristicValueType serviceCharacteristicValueType = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory()
        .createServiceCharacteristicValueType();

    serviceCharacteristicValueType.setValue(value);
    serviceCharacteristicValueType.setSscRef(namingAttributeType);

    return serviceCharacteristicValueType;
  }

  /**
   * Based on file:Dropbox/BOD/MTOSI/OneControl_R3 .0_MTOSI/DOCS/OneControl_R3.0_MTOSI_NBI.htm
   *
   * In tree 2 DataModel->UML->ServiceBasic-> TypeDefinitions->ServiceStateType
   */
  public static ReservationStatus mapToReservationState(ServiceStateType serviceState) {
    ReservationStatus reservationState = null;

    switch (serviceState) {
    case PLANNING_FEASIBILITY_CHECK:
      // Not used
      break;

    case PLANNING_DESIGNED:
      // Not used
      break;

    case PROVISIONED_ACTIVE:
      reservationState = ReservationStatus.RUNNING;
      break;

    case PROVISIONED_INACTIVE:
      reservationState = ReservationStatus.AUTO_START;
      break;

    case RESERVED:
      reservationState = ReservationStatus.SCHEDULED;
      break;

    case TERMINATED:
      reservationState = ReservationStatus.SUCCEEDED;
      break;
    }

    return reservationState;
  }
}

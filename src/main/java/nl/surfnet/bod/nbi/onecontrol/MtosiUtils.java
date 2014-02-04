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
package nl.surfnet.bod.nbi.onecontrol;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import nl.surfnet.bod.domain.ReservationStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmforum.mtop.fmw.xsd.msg.v1.BaseExceptionMessageType;
import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ActivateException;
import org.tmforum.mtop.sa.xsd.saiexcpt.v1.BasicFailureEventType;
import org.tmforum.mtop.sb.xsd.savc.v1.ServiceAttributeValueChangeType;
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceAccessPointType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceCharacteristicValueType;
import org.w3c.dom.Node;

public final class MtosiUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(MtosiUtils.class);

  private static final String PTP_FORMAT = "/rack=%s/shelf=%s/slot=%s/port=%s";
  private static final String PTP_WITH_SUB_SLOT_FORMAT = "/rack=%s/shelf=%s/slot=%s/sub_slot=%s/port=%s";

  private static final JAXBContext jaxbContext;
  private static final Logger logger = LoggerFactory.getLogger(MtosiUtils.class);

  static {
    try {
      jaxbContext = JAXBContext.newInstance("org.tmforum.mtop.sb.xsd.svc.v1");
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private MtosiUtils() {
  }

  public static void addHandlerToBinding(final Handler<?> handler, final BindingProvider bindingProvider) {
    @SuppressWarnings("rawtypes")
    final List<Handler> handlerChain = bindingProvider.getBinding().getHandlerChain();
    handlerChain.add(handler);
    bindingProvider.getBinding().setHandlerChain(handlerChain);
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

  public static Optional<String> findSscValue(final String sscValue, List<ServiceCharacteristicValueType> characteristics) {
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

  public static Optional<RfsSecondaryState> findSecondaryState(ServiceAttributeValueChangeType event) {
    Optional<ResourceFacingServiceType> rfs = findRfs(event);

    Optional<Optional<RfsSecondaryState>> status = rfs.transform(new Function<ResourceFacingServiceType, Optional<RfsSecondaryState>>() {
      @Override
      public Optional<RfsSecondaryState> apply(ResourceFacingServiceType service) {
        return findSecondaryState(service);
      }
    });

    return status.isPresent() ? status.get() : Optional.<RfsSecondaryState> absent();
  }

  public static Optional<ResourceFacingServiceType> findRfs(ServiceAttributeValueChangeType event) {
    if (event.getAttributeList() == null) {
      return Optional.absent();
    }

    Object any = event.getAttributeList().getAny();
    if (any instanceof Node) {
      try {
        return Optional.of(jaxbContext.createUnmarshaller().unmarshal((Node) any, ResourceFacingServiceType.class).getValue());
      } catch (JAXBException e) {
        logger.warn("Could not parse a RFS", e);
      }
    }

    return Optional.absent();
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
    return new RelativeDistinguishNameType().withType(type).withValue(value);
  }

  public static NamingAttributeType createRfs(String name) {
    return createNamingAttributeType("RFS", name);
  }

  public static ServiceCharacteristicValueType createSscValue(String name, String value) {
    return createSscValue(createNamingAttributeType("SSC", name), value);
  }

  public static NamingAttributeType createNamingAttributeType(String type, String value) {
    return new NamingAttributeType().withRdn(createRdn(type, value));
  }

  public static ServiceCharacteristicValueType createSecondaryStateValueType(String value) {
    return new ServiceCharacteristicValueType()
        .withSscRef(createNamingAttributeType("SSC", "SecondaryState"))
        .withValue(value);
  }

  public static JAXBElement<NamingAttributeType> createComonObjectInfoTypeName(String type, String value) {
    return new org.tmforum.mtop.fmw.xsd.coi.v1.ObjectFactory().createCommonObjectInfoTypeName(createNamingAttributeType(type, value));
  }

  public static ServiceCharacteristicValueType createSscValue(NamingAttributeType namingAttributeType, String value) {
    return new ServiceCharacteristicValueType().withValue(value).withSscRef(namingAttributeType);
  }

  /**
   * Based on file:Dropbox/BOD/MTOSI/OneControl_R3
   * .0_MTOSI/DOCS/OneControl_R3.0_MTOSI_NBI.htm
   *
   * In 14 Service Management Model -> ServiceStates
   *
   * We only look at the secondary status, since the service state is null when
   * receiving value attribute change notifications, even though this is not
   * allowed by the schema.
   */
  public static Optional<ReservationStatus> mapToReservationState(ResourceFacingServiceType rfs) {
    RfsSecondaryState secondaryState = findSecondaryState(rfs).or(RfsSecondaryState.UNKNOWN);
    LOGGER.debug("MAPPING status {} {}", rfs.getServiceState(), secondaryState);
    switch (secondaryState) {
    case RESERVING:
      return Optional.of(ReservationStatus.REQUESTED);
    case INITIAL:
      return Optional.of(ReservationStatus.RESERVED);
    case SCHEDULED:
      return Optional.of(ReservationStatus.SCHEDULED);
    case PROVISIONING:
      return Optional.of(ReservationStatus.AUTO_START);
    case ACTIVATING:
      return Optional.absent();
    case ACTIVATED:
      return Optional.of(ReservationStatus.RUNNING);
    case TERMINATING:
      return Optional.absent();
    case TERMINATED:
      return Optional.of(ReservationStatus.SUCCEEDED);
    case UNKNOWN:
      return Optional.absent();
    }

    throw new IllegalArgumentException("unrecognized reservation state <" + rfs.getServiceState() + ", " + secondaryState + "> for RFS " + rfs);
  }

  static Optional<RfsSecondaryState> findSecondaryState(ResourceFacingServiceType service) {
    return findSscValue("SecondaryState", service.getDescribedByList()).transform(
      new Function<String, RfsSecondaryState>() {
        @Override
        public RfsSecondaryState apply(String input) {
          try {
            return RfsSecondaryState.valueOf(input);
          } catch (IllegalArgumentException e) {
            LOGGER.warn("Unrecognized MTOSI secondary state " + input);
            return RfsSecondaryState.UNKNOWN;
          }
        }
      });
  }
}

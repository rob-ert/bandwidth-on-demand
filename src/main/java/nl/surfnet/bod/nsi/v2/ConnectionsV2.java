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
package nl.surfnet.bod.nsi.v2;

import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.nsi.NsiConstants;
import nl.surfnet.bod.util.JaxbUserType;
import nl.surfnet.bod.util.NsiV2Point2PointServiceUserType;

import org.ogf.schemas.nsi._2013._07.connection.types.QueryRecursiveResultType;
import org.ogf.schemas.nsi._2013._07.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._07.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._07.connection.types.ReservationRequestCriteriaType;
import org.ogf.schemas.nsi._2013._07.services.point2point.EthernetBaseType;
import org.ogf.schemas.nsi._2013._07.services.point2point.EthernetVlanType;
import org.ogf.schemas.nsi._2013._07.services.point2point.ObjectFactory;
import org.ogf.schemas.nsi._2013._07.services.point2point.P2PServiceBaseType;
import org.ogf.schemas.nsi._2013._07.services.types.StpType;
import org.w3c.dom.Element;

public final class ConnectionsV2 {

  private static final ObjectFactory P2P_OF = new ObjectFactory();

  public static final JaxbUserType<P2PServiceBaseType> P2PS_CONVERTER = new NsiV2Point2PointServiceUserType<>(P2P_OF.createP2Ps(null));
  public static final JaxbUserType<EthernetBaseType> ETS_CONVERTER = new NsiV2Point2PointServiceUserType<>(P2P_OF.createEts(null));
  public static final JaxbUserType<EthernetVlanType> EVTS_CONVERTER = new NsiV2Point2PointServiceUserType<>(P2P_OF.createEvts(null));
  private static final String P2P_NAMESPACE = P2PS_CONVERTER.getXmlRootElementName().getNamespaceURI();

  public static final Function<ConnectionV2, QuerySummaryResultType> toQuerySummaryResultType = new Function<ConnectionV2, QuerySummaryResultType>() {
    public QuerySummaryResultType apply(ConnectionV2 connection) {
      return connection.getQuerySummaryResult();
    }
  };

  public static final Function<ConnectionV2, QueryRecursiveResultType> toQueryRecursiveResultType =
      new Function<ConnectionV2, QueryRecursiveResultType>() {
        public QueryRecursiveResultType apply(ConnectionV2 connection) {
          return connection.getQueryRecursiveResult();
        }
      };

  public static String stpTypeToStpId(StpType type) {
    return type.getLocalId();
  }

  public static StpType toStpType(String localId) {
    return new StpType().withNetworkId(NsiConstants.URN_STP_V2).withLocalId(localId);
  }

  public static void addPointToPointService(Collection<Object> any, P2PServiceBaseType service) {
    Element element;
    if (service instanceof EthernetVlanType) {
      element = EVTS_CONVERTER.toDomElement((EthernetVlanType) service);
    } else if (service instanceof EthernetBaseType) {
      element = ETS_CONVERTER.toDomElement((EthernetBaseType) service);
    } else {
      element = P2PS_CONVERTER.toDomElement(service);
    }
    any.add(element);
  }

  public static Optional<P2PServiceBaseType> findPointToPointService(Collection<Object> any) {
    for (Object object : any) {
      if (object instanceof Element) {
        Element element = (Element) object;
        if (P2P_NAMESPACE.equals(element.getNamespaceURI())) {
          if (P2PS_CONVERTER.getXmlRootElementName().getLocalPart().equals(element.getLocalName())) {
            return Optional.of(P2PS_CONVERTER.fromDomElement(element));
          } else if (ETS_CONVERTER.getXmlRootElementName().getLocalPart().equals(element.getLocalName())) {
            return Optional.<P2PServiceBaseType> of(ETS_CONVERTER.fromDomElement(element));
          } else if (EVTS_CONVERTER.getXmlRootElementName().getLocalPart().equals(element.getLocalName())) {
            return Optional.<P2PServiceBaseType> of(EVTS_CONVERTER.fromDomElement(element));
          }
        }
      }
    }
    return Optional.absent();
  }

  public static Optional<P2PServiceBaseType> findPointToPointService(ReservationConfirmCriteriaType criteria) {
    return findPointToPointService(criteria.getAny());
  }

  public static Optional<P2PServiceBaseType> findPointToPointService(ReservationRequestCriteriaType criteria) {
    return findPointToPointService(criteria.getAny());
  }

  private ConnectionsV2() {
  }
}

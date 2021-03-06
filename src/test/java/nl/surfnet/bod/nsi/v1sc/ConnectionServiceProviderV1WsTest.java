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
package nl.surfnet.bod.nsi.v1sc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

import java.util.EnumSet;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.NsiV1RequestDetails;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.oauth.NsiScope;
import nl.surfnet.bod.nsi.ConnectionServiceProviderError;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.repo.ConnectionV1Repo;
import nl.surfnet.bod.support.ConnectionV1Factory;
import nl.surfnet.bod.support.NsiV1RequestDetailsFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.hamcrest.text.IsEmptyString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.BandwidthType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveType;
import org.ogf.schemas.nsi._2011._10.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceProviderV1WsTest {

  @InjectMocks
  private ConnectionServiceProviderV1Ws subject;

  @Mock private ConnectionV1Repo connectionRepoMock;
  @Mock private ConnectionServiceV1 connectionServiceProviderComponentMock;

  private NsiHelper dummyNsiHelper = new NsiHelper("", "", "", "", "");

  private final String nsaProvider = "nsa:surfnet.nl";

  private final RichUserDetails userDetails = new RichUserDetailsFactory().addUserGroup("test").create();

  private final ConnectionV1 connection = new ConnectionV1Factory().setSourceStpId("Source Port").setDestinationStpId(
      "Destination Port").setProviderNsa(nsaProvider).create();

  private final NsiV1RequestDetails request = new NsiV1RequestDetailsFactory().create();

  @Test
  public void shouldCreateReservation() throws ServiceException {
    subject.reserve(connection, request, userDetails);
  }

  @Test
  public void reserveTypeWithoutGlobalReservationIdShouldGetOne() {
    ReserveRequestType reserveRequestType = createReservationRequestType(1000, Optional.<String> absent());

    ConnectionV1 connection = ConnectionServiceProviderFunctions.reserveRequestToConnection(dummyNsiHelper, ProtectionType.PROTECTED).apply(reserveRequestType);

    assertThat(connection.getGlobalReservationId(), not(IsEmptyString.isEmptyOrNullString()));
    assertThat(connection.getDesiredBandwidth(), is(1000L));
  }

  @Test
  public void reserveTypeWithGlobalReservationId() {
    ReserveRequestType reserveRequestType = createReservationRequestType(1000, Optional.of("urn:surfnet.nl:12345"));

    ConnectionV1 connection = ConnectionServiceProviderFunctions.reserveRequestToConnection(dummyNsiHelper, ProtectionType.PROTECTED).apply(reserveRequestType);

    assertThat(connection.getGlobalReservationId(), is("urn:surfnet.nl:12345"));
  }

  @Test
  public void reserveWithoutReserveScopeShouldFail() throws ServiceException {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.of(NsiScope.QUERY)).create());

    try {
      subject.reserve(createReservationRequestType(100, Optional.of("1234")));
      fail("Exception expected");
    } catch (ServiceException e) {
      assertThat(e.getFaultInfo().getErrorId(), is(ConnectionServiceProviderError.UNAUTHORIZED.getErrorId()));
    }
  }

  @Test
  public void queryWithoutReserveScopeShouldFail() throws ServiceException {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.of(NsiScope.RESERVE)).create());

    try {
      subject.query(new QueryRequestType());
      fail("Exception expected");
    } catch (ServiceException e) {
      assertThat(e.getFaultInfo().getErrorId(), is(ConnectionServiceProviderError.UNAUTHORIZED.getErrorId()));
    }
  }


  public static ReserveRequestType createReservationRequestType(int desiredBandwidth, Optional<String> globalReservationId) {
    ServiceTerminationPointType dest = new ServiceTerminationPointType();
    dest.setStpId("urn:stp:1");

    ServiceTerminationPointType source = new ServiceTerminationPointType();
    source.setStpId("urn:stp:1");

    PathType path = new PathType();
    path.setDestSTP(dest);
    path.setSourceSTP(source);

    ScheduleType schedule = new ScheduleType();
    XMLGregorianCalendar xmlGregorianCalendar;
    try {
      xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar();
      schedule.setStartTime(xmlGregorianCalendar);
      schedule.setEndTime(xmlGregorianCalendar);
    } catch (DatatypeConfigurationException e) {
      Throwables.propagate(e);
    }

    BandwidthType bandwidth = new BandwidthType();
    bandwidth.setDesired(desiredBandwidth);

    ServiceParametersType serviceParameters = new ServiceParametersType();
    serviceParameters.setSchedule(schedule);
    serviceParameters.setBandwidth(bandwidth);

    ReservationInfoType reservation = new ReservationInfoType();
    reservation.setPath(path);
    reservation.setServiceParameters(serviceParameters);
    reservation.setGlobalReservationId(globalReservationId.orNull());

    ReserveType reserve = new ReserveType();
    reserve.setReservation(reservation);

    ReserveRequestType reserveRequestType = new ReserveRequestType();
    reserveRequestType.setReserve(reserve);

    return reserveRequestType;
  }

}

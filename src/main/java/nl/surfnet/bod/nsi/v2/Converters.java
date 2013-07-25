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

import nl.surfnet.bod.util.JaxbUserType;
import nl.surfnet.bod.util.NsiV2UserType;

import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStateChangeRequestType;
import org.ogf.schemas.nsi._2013._04.connection.types.ErrorEventType;
import org.ogf.schemas.nsi._2013._04.connection.types.GenericConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.GenericFailedType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryNotificationConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryRecursiveConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReserveConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReserveTimeoutRequestType;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;

class Converters {
  private static final org.ogf.schemas.nsi._2013._04.framework.headers.ObjectFactory HEADER_OF = new org.ogf.schemas.nsi._2013._04.framework.headers.ObjectFactory();
  private static final org.ogf.schemas.nsi._2013._04.connection.types.ObjectFactory BODY_OF = new org.ogf.schemas.nsi._2013._04.connection.types.ObjectFactory();

  public static final JaxbUserType<CommonHeaderType> COMMON_HEADER_CONVERTER = new NsiV2UserType<>(HEADER_OF.createNsiHeader(null));
  public static final JaxbUserType<ReserveConfirmedType> RESERVE_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createReserveConfirmed(null));
  public static final JaxbUserType<GenericFailedType> RESERVE_FAILED_CONVERTER = new NsiV2UserType<>(BODY_OF.createReserveFailed(null));
  public static final JaxbUserType<ReserveTimeoutRequestType> RESERVE_TIMEOUT_CONVERTER = new NsiV2UserType<>(BODY_OF.createReserveTimeout(null));
  public static final JaxbUserType<GenericConfirmedType> RESERVE_COMMIT_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createReserveCommitConfirmed(null));
  public static final JaxbUserType<GenericConfirmedType> RESERVE_ABORT_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createReserveAbortConfirmed(null));
  public static final JaxbUserType<GenericConfirmedType> TERMINATE_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createTerminateConfirmed(null));
  public static final JaxbUserType<GenericConfirmedType> PROVISION_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createProvisionConfirmed(null));
  public static final JaxbUserType<DataPlaneStateChangeRequestType> DATA_PLANE_STATE_CHANGE_CONVERTER = new NsiV2UserType<>(BODY_OF.createDataPlaneStateChange(null));
  public static final JaxbUserType<ErrorEventType> ERROR_EVENT_CONVERTER = new NsiV2UserType<>(BODY_OF.createErrorEvent(null));
  public static final JaxbUserType<QuerySummaryConfirmedType> QUERY_SUMMARY_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createQuerySummaryConfirmed(null));
  public static final JaxbUserType<QueryRecursiveConfirmedType> QUERY_RECURSIVE_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createQueryRecursiveConfirmed(null));
  public static final JaxbUserType<QueryNotificationConfirmedType> QUERY_NOTIFICATION_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createQueryNotificationConfirmed(null));

}

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
package nl.surfnet.bod.web.appmanager;

import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;

import java.util.List;

import javax.annotation.Resource;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.ConnectionServiceV1;
import nl.surfnet.bod.web.appmanager.ConnectionController.ConnectionView;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/appmanager/connections")
public class ConnectionController extends AbstractSearchableSortableListController<ConnectionView, ConnectionV1> {

  @Resource private ConnectionServiceV1 connectionService;

  @RequestMapping("/illegal")
  public String listIllegal(
    @RequestParam(value = PAGE_KEY, required = false) Integer page,
    @RequestParam(value = "sort", required = false) String sort,
    @RequestParam(value = "order", required = false) String order,
    Model model) {

    Sort sortOptions = prepareSortOptions(sort, order, model);

    model.addAttribute("list", transformToView(
      connectionService.findWithIllegalState(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions),
      Security.getUserDetails())
    );

    return listUrl() + "/illegal";
  }

  @Override
  protected List<ConnectionView> transformToView(List<? extends ConnectionV1> entities, RichUserDetails user) {
    return Lists.transform(entities, new Function<ConnectionV1, ConnectionView>() {
      @Override
      public ConnectionView apply(ConnectionV1 connection) {
        return new ConnectionView(connection);
      }
    });
  }

  @Override
  protected String listUrl() {
    return "appmanager/connections/list";
  }

  @Override
  protected List<ConnectionView> list(int firstPage, int maxItems, Sort sort, Model model) {
    return transformToView(connectionService.findEntries(firstPage, maxItems, sort), Security.getUserDetails());
  }

  @Override
  protected long count(Model model) {
    return connectionService.count();
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort) {
    return connectionService.findIds(Optional.<Sort> fromNullable(sort));
  }

  @Override
  protected AbstractFullTextSearchService<ConnectionV1> getFullTextSearchableService() {
    return connectionService;
  }

  @Override
  protected String getDefaultSortProperty() {
    return "startTime";
  }

  @Override
  protected List<String> translateSortProperty(String sortProperty) {
    if ("nsiStatus".equals(sortProperty)) {
      return ImmutableList.of("currentState");
    }
    if ("reservationStatus".equals(sortProperty)) {
      return ImmutableList.of("reservation.status");
    }

    return super.translateSortProperty(sortProperty);
  }

  public static class ConnectionView {
    private final String description;
    private final String label;
    private final ConnectionStateType nsiStatus;
    private final ReservationStatus reservationStatus;
    private final Optional<DateTime> startTime;
    private final Optional<DateTime> endTime;
    private final Long id;
    private final String reservationId;

    public ConnectionView(ConnectionV1 connection) {
      this.id = connection.getId();
      this.description = connection.getDescription();
      this.label = connection.getLabel();
      this.nsiStatus = connection.getCurrentState();
      this.reservationStatus = connection.getReservation() == null ? null : connection.getReservation().getStatus();
      this.reservationId = connection.getReservation() == null ? null : connection.getReservation().getReservationId();
      this.startTime = connection.getStartTime();
      this.endTime = connection.getEndTime();
    }

    public String getLabel() {
      return label;
    }

    public ConnectionStateType getNsiStatus() {
      return nsiStatus;
    }

    public ReservationStatus getReservationStatus() {
      return reservationStatus;
    }

    public Optional<DateTime> getStartTime() {
      return startTime;
    }

    public Optional<DateTime> getEndTime() {
      return endTime;
    }

    public String getDescription() {
      return description;
    }

    public Long getId() {
      return id;
    }

    public String getReservationId() {
      return reservationId;
    }
  }

}
/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.web.manager;

import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;

import java.util.List;

import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractFilteredReservationController;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationFilterView;
import nl.surfnet.bod.web.view.ReservationView;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("managerReservationController")
@RequestMapping(ReservationController.PAGE_URL)
public class ReservationController extends AbstractFilteredReservationController {

  public static final String PAGE_URL = "manager/reservations";
  public static final String ELAPSED_URL = PAGE_URL + "/" + FILTER_URL + ReservationFilterViewFactory.ELAPSED;
  public static final String ACTIVE_URL = PAGE_URL + "/" + FILTER_URL + ReservationFilterViewFactory.ACTIVE;
  public static final String COMING_URL = PAGE_URL + "/" + FILTER_URL + ReservationFilterViewFactory.COMING;

  @Override
  protected List<? extends ReservationView> list(int firstPage, int maxItems, Sort sort, Model model) {
    ReservationFilterView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);

   return transformToView(
        getReservationService().findEntriesForManagerUsingFilter(Security.getUserDetails(), filter, firstPage, maxItems, sort),
        Security.getUserDetails());
  }

  @Override
  protected long count(Model model) {
    ReservationFilterView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);
    return getReservationService().countForFilterAndManager(Security.getUserDetails(), filter);
  }

  @Override
  public String listUrl() {
    return PAGE_URL + WebUtils.LIST;
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort) {
    ReservationFilterView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);
    return getReservationService().findIdsForManagerUsingFilter(Security.getUserDetails(), filter, sort);
  }

}

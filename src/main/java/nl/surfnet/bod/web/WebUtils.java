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
package nl.surfnet.bod.web;

import static org.springframework.util.StringUtils.hasText;
import nl.surfnet.bod.web.security.Security;

import org.joda.time.Hours;
import org.joda.time.ReadablePeriod;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;

import com.google.common.base.Optional;

public final class WebUtils {

  public static final String CREATE = "/create";
  public static final String EDIT = "/edit";
  public static final String UPDATE = "/update";
  public static final String DELETE = "/delete";
  public static final String LIST = "/list";

  /**
   * Used to distinguish between the model and a listmodel.
   */
  public static final String LIST_POSTFIX = "List";
  public static final String MAX_PAGES_KEY = "maxPages";
  public static final String PAGE_KEY = "page";
  public static final String ID_KEY = "id";
  public static final String FILTER_LIST = "filterList";
  public static final String FILTER_SELECT = "filterSelect";
  public static final String DATA_LIST = "list";

  public static final int MAX_ITEMS_PER_PAGE = 15;

  public static final String PARAM_MARKUP_START = "<b>";
  public static final String PARAM_MARKUP_END = "</b>";
  public static final String PARAM_SEARCH = "search";

  public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd H:mm:ss";
  public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormat
      .forPattern(DEFAULT_DATE_TIME_PATTERN);

  public static final ReadablePeriod DEFAULT_RESERVATON_DURATION = Hours.FOUR;

  private WebUtils() {
  }

  public static int calculateFirstPage(Integer page) {
    return (page == null || page == 0) ? 0 : (page.intValue() - 1) * MAX_ITEMS_PER_PAGE;
  }

  public static int calculateMaxPages(long totalEntries) {
    float nrOfPages = (float) totalEntries / MAX_ITEMS_PER_PAGE;
    return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
  }

  public static <T> T getAttributeFromModel(String attributeName, Model model) {
    return getAttributeFromModel(attributeName, model, null);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getAttributeFromModel(String attributeName, Model model, T defaultValue) {
    return (T) (model.asMap().get(attributeName) == null ? defaultValue : model.asMap().get(attributeName));
  }

  public static Optional<Long> getSelectedPhysicalResourceGroupId() {
    return Security.getUserDetails().getSelectedRole() == null ? Optional.<Long> absent() : Security.getUserDetails()
        .getSelectedRole().getPhysicalResourceGroupId();
  }

  public static boolean not(boolean expression) {
    return !expression;
  }

  public static String shortenAdminGroup(String adminGroup) {
    int index = -1;
    if (hasText(adminGroup)) {
      index = adminGroup.lastIndexOf(':');
    }

    if (index >= 0) {
      return adminGroup.substring(index + 1);
    }
    return adminGroup;
  }

  /**
   * Replaces the logicalName with the given technialName in the searchString
   * E.g. 'name:test' might be mapped to 'virtualResourceGroup.name:test'
   *
   * @param searchString
   *          String to replace
   * @param logicalName
   *          String to search for
   * @param technicalName
   *          String to replace with
   * @return String When logicalName is contained in the searchString the
   *         replaced version, otherwise the unchanged searchString
   */
  public static String replaceSearchWith(String searchString, String logicalName, String technicalName) {
    if (hasText(logicalName) && hasText(technicalName) && hasText(searchString) && searchString.contains(logicalName)) {
      return searchString.replace(logicalName, technicalName);
    }
    return searchString;
  }

  public static PageRequest createPageRequest(int firstResult, int maxResults, Sort sort) {
    return new PageRequest(firstResult / maxResults, maxResults, sort);
  }

}
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

import java.util.List;

import javax.annotation.Resource;

import com.google.common.collect.FluentIterable;

import nl.surfnet.bod.nbi.onecontrol.NotificationConsumerHttp;
import nl.surfnet.bod.web.base.MessageManager;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Profile("onecontrol")
@Controller
@RequestMapping("/appmanager/onecontrol")
public class OneControlNotificationsController {

  @Resource private NotificationConsumerHttp notificationConsumer;
  @Resource private MessageManager messageManager;

  @RequestMapping
  public String index() {
    return "appmanager/onecontrol/index";
  }

  @RequestMapping("/notifications")
  public String listNotifications(Model model) {
    model.addAttribute("alarms", last(notificationConsumer.getAlarms(), 20));
    model.addAttribute("serviceObjectCreations", last(notificationConsumer.getServiceObjectCreations(), 20));
    model.addAttribute("serviceObjectDeletions", last(notificationConsumer.getServiceObjectDeletions(), 20));
    model.addAttribute("serviceAttributeValueChanges", last(notificationConsumer.getServiceAttributeValueChanges(), 20));
    model.addAttribute("events", last(notificationConsumer.getEvents(), 20));

    return "appmanager/onecontrol/notifications";
  }

  protected <T> List<T> last(List<T> collection, int size) {
    if (collection.size() > size) {
      return FluentIterable.from(collection).skip(collection.size() - size).toList();
    }

    return collection;
  }
}
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

import nl.surfnet.bod.nbi.mtosi.MtosiNotificationLiveClient;
import nl.surfnet.bod.nbi.mtosi.MtosiNotificationLiveClient.NotificationTopic;
import nl.surfnet.bod.nbi.mtosi.NotificationConsumerHttp;
import nl.surfnet.bod.web.base.MessageManager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.SubscribeException;

import com.google.common.collect.FluentIterable;

@Controller
@RequestMapping("/appmanager/mtosi")
public class MtosiNotificationsController {

  @Resource
  private NotificationConsumerHttp notificationConsumer;

  @Resource
  private MtosiNotificationLiveClient notificationClient;

  @Resource
  private MessageManager messageManager;

  @RequestMapping
  public String index() {
    return "appmanager/mtosi/index";
  }

  @RequestMapping("/notifications")
  public String listNotifications(Model model) {
    model.addAttribute("heartbeats", last(notificationConsumer.getHeartbeats(), 20));
    model.addAttribute("alarms", last(notificationConsumer.getAlarms(), 20));

    return "appmanager/mtosi/notifications";
  }

  @RequestMapping(value = "/notifications/subscribe", method = RequestMethod.POST)
  public String subscribe(String topic, String consumer, RedirectAttributes redirectAttributes) {
    try {
      String subscribe = notificationClient.subscribe(NotificationTopic.valueOf(topic), consumer);
      messageManager.addInfoFlashMessage(redirectAttributes, "Subscribed to topic {}", subscribe);

    }
    catch (SubscribeException e) {
      messageManager.addErrorFlashMessage(redirectAttributes, "Failed to subscribe");
    }

    return "redirect:appmanager/mtosi";
  }

  protected <T> List<T> last(List<T> collection, int size) {
    if (collection.size() > size) {
      return FluentIterable.from(collection).skip(collection.size() - size).toList();
    }

    return collection;
  }
}
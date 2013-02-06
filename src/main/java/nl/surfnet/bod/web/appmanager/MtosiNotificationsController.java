package nl.surfnet.bod.web.appmanager;

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.nbi.mtosi.NotificationConsumerHttp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.FluentIterable;

@Controller
@RequestMapping("/appmanager/mtosi")
public class MtosiNotificationsController {

  @Resource
  private NotificationConsumerHttp notificationConsumer;

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

  protected <T> List<T> last(List<T> collection, int size) {
    if (collection.size() > size) {
      return FluentIterable.from(collection).skip(collection.size() - size).toList();
    }

    return collection;
  }
}
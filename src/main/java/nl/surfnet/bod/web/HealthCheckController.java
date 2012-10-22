/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.web;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.idd.IddClient;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.service.GroupService;
import nl.surfnet.bod.util.Environment;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Lists;

@Controller
public class HealthCheckController {

  @Resource
  private IddClient iddClient;

  @Resource
  private NbiClient nbiClient;

  @Resource
  private GroupService groupService;

  @Resource
  private Environment env;

  @RequestMapping(value = "/healthcheck")
  public String index(Model model) {

    final ServiceCheck iddServiceCheck = new ServiceCheck() {
      @Override
      public boolean healty() {
        return iddClient.getKlanten().size() > 0;
      }
    };

    final ServiceCheck nbiServiceCheck = new ServiceCheck() {
      @Override
      public boolean healty() {
        return nbiClient.getPhysicalPortsCount() > 0;
      }
    };

    final ServiceCheck oAuthServerServiceCheck = new ServiceCheck() {
      @Override
      public boolean healty() throws IOException {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(env.getOauthServerUrl() + "/admin");
        HttpResponse response = client.execute(httpGet);
        httpGet.releaseConnection();
        return response.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN;
      }
    };

    final ServiceCheck apiServiceCheck = new ServiceCheck() {
      @Override
      public boolean healty() throws IOException {
        Collection<UserGroup> groups = groupService.getGroups("urn:collab:person:surfguest.nl:alanvdam");
        return groups.size() > 0;
      }
    };

    @SuppressWarnings("unchecked")
    List<Callable<Boolean>> tasks = Lists.newArrayList(
        callable(iddServiceCheck),
        callable(nbiServiceCheck),
        callable(oAuthServerServiceCheck),
        callable(apiServiceCheck)
    );

    try {
      List<Future<Boolean>> futures = Executors.newFixedThreadPool(4).invokeAll(tasks, 15, TimeUnit.SECONDS);

      model.addAttribute("iddHealth", toBooleanValue(futures.get(0)));
      model.addAttribute("nbiHealth", toBooleanValue(futures.get(1)));
      model.addAttribute("oAuthServer", toBooleanValue(futures.get(2)));
      model.addAttribute("openConextApi", toBooleanValue(futures.get(3)));
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }

    return "healthcheck";
  }

  private boolean toBooleanValue(Future<Boolean> healthFuture) {
    if (healthFuture.isCancelled()) {
      return false;
    }

    try {
      return healthFuture.isDone() && healthFuture.get();
    }
    catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      return false;
    }
  }

  private Callable<Boolean> callable(final ServiceCheck serviceCheck) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return isServiceHealthy(serviceCheck);
      }
    };
  }

  public boolean isServiceHealthy(ServiceCheck check) {
    try {
      return check.healty();
    }
    catch (Exception e) {
      return false;
    }
  }

  interface ServiceCheck {
    boolean healty() throws Exception;
  }
}

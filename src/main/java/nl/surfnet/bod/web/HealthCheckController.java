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

import static nl.surfnet.bod.web.HealthCheckController.ServiceState.DISABLED;
import static nl.surfnet.bod.web.HealthCheckController.ServiceState.FAILED;
import static nl.surfnet.bod.web.HealthCheckController.ServiceState.SUCCEEDED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import nl.surfnet.bod.idd.IddClient;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.service.GroupService;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.VersReportingService;
import nl.surfnet.bod.util.Environment;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HealthCheckController {

  private Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

  @Resource
  private IddClient iddClient;

  @Resource
  private InstituteService instituteService;

  @Resource
  private NbiClient nbiClient;

  @Resource
  private GroupService openSocialGroupService;

  @Resource
  private GroupService sabGroupService;

  @Resource
  private VersReportingService verseReportingService;

  @Resource(name = "bodEnvironment")
  private Environment environment;

  public interface ServiceCheck {
    ServiceState healty() throws Exception;

    String getName();
  }

  private final ServiceCheck iddServiceCheck = new ServiceCheck() {
    @Override
    public ServiceState healty() {
      Instant lastUpdatedAt = instituteService.instituteslastUpdatedAt();
      Duration timeout = environment.getInstituteCacheMaxAge();
      return (lastUpdatedAt != null && lastUpdatedAt.plus(timeout.getMillis()).isAfterNow()) ? SUCCEEDED : FAILED;
    }

    @Override
    public String getName() {
      return "IDD (using " + iddClient.getClass().getSimpleName() + ")";
    }
  };

  private final ServiceCheck nbiServiceCheck = new ServiceCheck() {
    @Override
    public ServiceState healty() {
      return nbiClient.getPhysicalPortsCount() > 0 ? SUCCEEDED : FAILED;
    }

    @Override
    public String getName() {
      return "NBI (using " + nbiClient.getClass().getSimpleName() + ")";
    }
  };

  private final ServiceCheck oAuthServerServiceCheck = new ServiceCheck() {
    @Override
    public ServiceState healty() throws IOException {
      DefaultHttpClient client = new DefaultHttpClient();
      HttpGet httpGet = new HttpGet(environment.getOauthServerUrl() + "/admin");
      HttpResponse response = client.execute(httpGet);
      httpGet.releaseConnection();
      return response.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN ? SUCCEEDED : FAILED;
    }

    @Override
    public String getName() {
      return "OAuth server";
    }
  };

  private final ServiceCheck apiServiceCheck = new ServiceCheck() {
    private static final String PERSON_URI = "urn:collab:person:surfnet.nl:hanst";

    @Override
    public ServiceState healty() throws IOException {
      return openSocialGroupService.getGroups(PERSON_URI).size() > 0 ? SUCCEEDED : FAILED;
    }

    @Override
    public String getName() {
      return "API (groupService)";
    }
  };

  private final ServiceCheck sabServiceCheck = new ServiceCheck() {
    private static final String PERSON_URI = "urn:collab:person:surfnet.nl:hanst";

    @Override
    public ServiceState healty() throws IOException {
      if (environment.isSabEnabled()) {
        return sabGroupService.getGroups(PERSON_URI).size() > 0 ? SUCCEEDED : FAILED;
      }
      else {
        return DISABLED;
      }
    }

    @Override
    public String getName() {
      return "SAB (groupService)";
    }
  };

  private final ServiceCheck versCheck = new ServiceCheck() {
    @Override
    public ServiceState healty() throws IOException {
      return verseReportingService.isWsdlAvailable() ? SUCCEEDED : FAILED;
    };

    @Override
    public String getName() {
      return "VERS (reporting service)";
    }
  };

  private List<ServiceCheck> checks = Arrays.asList(
      iddServiceCheck,
      nbiServiceCheck,
      oAuthServerServiceCheck,
      apiServiceCheck,
      sabServiceCheck,
      versCheck);

  @RequestMapping(value = "/healthcheck")
  public String index(Model model, HttpServletResponse response) {
    List<Callable<ServiceCheckResult>> tasks = new ArrayList<>();
    for (ServiceCheck check : checks) {
      tasks.add(callable(check));
    }

    boolean everythingOk = true;
    ExecutorService threadPool = Executors.newFixedThreadPool(tasks.size());
    List<ServiceCheckResult> systems = new ArrayList<>();
    try {
      List<Future<ServiceCheckResult>> futures = threadPool.invokeAll(tasks, 20, TimeUnit.SECONDS);
      for (int i = 0; i < futures.size(); ++i) {
        systems.add(toState(checks.get(i).getName(), futures.get(i)));
      }
      model.addAttribute("systems", systems);

      for (ServiceCheckResult result : systems) {
        everythingOk &= result.state != ServiceState.FAILED;
      }
    } catch (InterruptedException e) {
      everythingOk = false;
      logger.error("Error during calling healthchecks", e);
    } finally {
      threadPool.shutdown();
    }
    response.setStatus(everythingOk ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    return "healthcheck";
  }

  private ServiceCheckResult toState(String name, Future<ServiceCheckResult> healthFuture) {
    try {
      return healthFuture.get();
    } catch (InterruptedException | ExecutionException | CancellationException e) {
      logger.error("Error during healthcheck of " + name, e);
      return new ServiceCheckResult(name, ServiceState.FAILED);
    }

  }

  private Callable<ServiceCheckResult> callable(final ServiceCheck serviceCheck) {
    return new Callable<ServiceCheckResult>() {
      @Override
      public ServiceCheckResult call() {
        return new ServiceCheckResult(serviceCheck.getName(), isServiceHealthy(serviceCheck));
      }
    };
  }

  public ServiceState isServiceHealthy(ServiceCheck check) {
    ServiceState result;
    try {
      result = check.healty();

      if (result.failed()) {
        logger.error("HealthCheck for '{}' failed", check.getName());
      }

    } catch (Exception e) {
      logger.error("Healthcheck for " + check.getName() + " failed with an exception: ", e);
      result = ServiceState.FAILED;
    }

    return result;
  }

  protected void setLogger(Logger logger) {
    this.logger = logger;
  }

  public void setChecks(List<ServiceCheck> checks) {
    this.checks = checks;
  }

  public enum ServiceState {
    FAILED, DISABLED, SUCCEEDED;

    public boolean failed() {
      return this == FAILED;
    }
  }

  public static class ServiceCheckResult {
    private String name;
    private ServiceState state;

    public ServiceCheckResult(String name, ServiceState state) {
      this.name = name;
      this.state = state;
    }

    public String getName() {
      return name;
    }

    public ServiceState getState() {
      return state;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((state == null) ? 0 : state.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ServiceCheckResult other = (ServiceCheckResult) obj;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      if (state != other.state)
        return false;
      return true;
    }
  }
}
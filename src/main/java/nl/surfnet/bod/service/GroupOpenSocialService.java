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
package nl.surfnet.bod.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.util.Environment;

import org.opensocial.Client;
import org.opensocial.RequestException;
import org.opensocial.auth.AuthScheme;
import org.opensocial.auth.OAuth2LeggedScheme;
import org.opensocial.models.Group;
import org.opensocial.providers.Provider;
import org.opensocial.providers.ShindigProvider;
import org.opensocial.services.GroupsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * More information about the OpenConext-api {@linkplain https://wiki.surfnetlabs.nl/display/conextdocumentation/REST+interface}
 */
@Service("openSocialGroupService")
public class GroupOpenSocialService implements GroupService {

  private final Logger logger = LoggerFactory.getLogger(GroupOpenSocialService.class);

  @Resource(name = "bodEnvironment")
  private Environment env;

  @Override
  public Collection<UserGroup> getGroups(String nameId) {
    try {
      List<Group> osGroups = getClient(nameId).send(GroupsService.getGroups(nameId)).getEntries();

      return Lists.newArrayList(Lists.transform(osGroups, new Function<Group, UserGroup>() {
        @Override
        public UserGroup apply(Group input) {
          return new UserGroup(input.getId(), input.getTitle(), input.getDescription());
        }
      }));
    }
    catch (RequestException e) {
      logger.error("Could not retreive groups from open social server", e);
      return Collections.emptyList();
    }
    catch (IOException e) {
      logger.error("Could not retreive groups from open social server", e);
      return Collections.emptyList();
    }
  }

  private Client getClient(String loggedInUser) {
    checkNotNull(emptyToNull(env.getOpenSocialOAuthKey()));
    checkNotNull(emptyToNull(env.getOpenSocialOAuthSecret()));
    checkNotNull(emptyToNull(env.getOpenSocialUrl()));

    Provider provider = new ShindigProvider(true);
    provider.setRestEndpoint(getEndPointUrl());
    provider.setVersion("0.9");

    AuthScheme scheme = new OAuth2LeggedScheme(env.getOpenSocialOAuthKey(), env.getOpenSocialOAuthSecret(),
        loggedInUser);

    return new Client(provider, scheme);
  }

  private String getEndPointUrl() {
    String url = env.getOpenSocialUrl();
    return url.endsWith("/") ? url : url.concat("/");
  }

  protected void setEnvironment(Environment environment) {
    this.env = environment;
  }

}
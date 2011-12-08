package nl.surfnet.bod.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import nl.surfnet.bod.util.Environment;

import org.opensocial.Client;
import org.opensocial.RequestException;
import org.opensocial.auth.AuthScheme;
import org.opensocial.auth.OAuth2LeggedScheme;
import org.opensocial.models.Group;
import org.opensocial.providers.Provider;
import org.opensocial.providers.ShindigProvider;
import org.opensocial.services.GroupsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupOpenSocialService implements GroupService {

  @Autowired
  private Environment env;

  @Override
  public Collection<Group> getGroups(String nameId) {
    try {
      return getClient(nameId).send(GroupsService.getGroups()).getEntries();
    }
    catch (RequestException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
    catch (IOException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  protected Client getClient(String loggedInUser) {
    Provider provider = new ShindigProvider(true);

    provider.setRestEndpoint(env.getOpenSocialUrl() + "/rest/");
    provider.setRpcEndpoint(null);
    provider.setVersion("0.9");

    AuthScheme scheme = new OAuth2LeggedScheme(env.getOpenSocialOAuthKey(), env.getOpenSocialOAuthSecret(),
        loggedInUser);

    return new Client(provider, scheme);
  }
}

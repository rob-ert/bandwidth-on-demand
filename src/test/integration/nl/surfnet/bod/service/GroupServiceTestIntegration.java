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
package nl.surfnet.bod.service;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.opensocial.Client;
import org.opensocial.Request;
import org.opensocial.RequestException;
import org.opensocial.auth.AuthScheme;
import org.opensocial.auth.OAuth2LeggedScheme;
import org.opensocial.models.Group;
import org.opensocial.models.Model;
import org.opensocial.models.Person;
import org.opensocial.providers.Provider;
import org.opensocial.providers.ShindigProvider;

public class GroupServiceTestIntegration {

  private static final String REST_TEMPLATE_GROUPS = "groups/{guid}";
  private static final String REST_TEMPLATE_PEOPLE = "people/{guid}/{selector}/{pid}";
  private static final String ME = "@me";
  private static final String openSocialUrl = "https://os.surfconext.nl/social";
//  private static final String openSocialUrl = "https://os.test.surfconext.nl/social/";

  private static final String oAuthKey = "https://atlas.dlp.surfnet.nl/bod/test";
//  private static final String oAuthKey = "https://teams.test.surfconext.nl/teams/teams.xml";
  private static final String oAuthSecret = "1dsf#jes4z8wz35xdABgcfy";
//  private static final String oAuthSecret = "mysecret";

//  private final String loggedInUser = "urn:collab:person:surfguest.nl:oharsta";
  private final String loggedInUser = "urn:collab:person:surfguest.nl:alanvdam";
  private final String groupId = "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:managementvo";

  @Test
  public void getGroups() throws RequestException, IOException {
    Request request = new Request(REST_TEMPLATE_GROUPS, "groups.get", "GET");
    request.setModelClass(Group.class);
    request.setGuid(ME);

    List<Model> entries = getClient(loggedInUser).send(request).getEntries();

    System.err.println(entries);
  }

//  @Test
  public void doesGroupExist() throws RequestException, IOException {
    Request request = new Request(REST_TEMPLATE_PEOPLE, "people.get", "GET");
    request.setModelClass(Person.class);
    request.setSelector(groupId);
    request.setGuid(loggedInUser);

    List<Model> entries = getClient(loggedInUser).send(request).getEntries();

    System.err.println(entries);
  }

  protected Client getClient(String loggedInUser) {
    Provider provider = new ShindigProvider(true);

    provider.setRestEndpoint(openSocialUrl + "/rest/");
    provider.setRpcEndpoint(null);
    provider.setVersion("0.9");

    AuthScheme scheme = new OAuth2LeggedScheme(oAuthKey, oAuthSecret, loggedInUser);

    return new Client(provider, scheme);
  }
}
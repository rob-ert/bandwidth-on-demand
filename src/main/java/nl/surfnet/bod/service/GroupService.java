package nl.surfnet.bod.service;

import java.util.Collection;

import org.opensocial.models.Group;

public interface GroupService {

  Collection<Group> getGroups(String nameId);
}

package nl.surfnet.bod.service;

import java.util.Collection;

import nl.surfnet.bod.domain.UserGroup;

public interface GroupService {

  Collection<UserGroup> getGroups(String nameId);
}

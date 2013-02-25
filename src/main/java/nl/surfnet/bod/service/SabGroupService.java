package nl.surfnet.bod.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.sabng.EntitlementsHandler;

import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

@Service("sabGroupService")
public class SabGroupService implements GroupService {
  @VisibleForTesting
  static final String GROUP_PREFIX = "urn:collab:person:sab:admin:bod.surfnet.nl:";
  static final String NAME_PREFIX = "BoD Administrator ";
  static final String DESCRIPTION_PREFIX = NAME_PREFIX + " of ";

  @Resource
  private EntitlementsHandler entitlementsHandler;

  @Override
  public Collection<UserGroup> getGroups(String nameId) {
    List<UserGroup> groups = new ArrayList<>();

    for (String institute : entitlementsHandler.checkInstitutes(nameId)) {
      UserGroup userGroup = new UserGroup(composeGroupName(institute), composeName(institute),
          composeDescription(institute));
      userGroup.setInstituteShortName(Optional.<String> of(institute));

      groups.add(userGroup);
    }

    return groups;
  }

  @VisibleForTesting
  String composeGroupName(String instituteName) {
    Preconditions.checkNotNull(instituteName);
    return GROUP_PREFIX.concat(instituteName.toLowerCase());
  }

  @VisibleForTesting
  String composeName(String instituteName) {
    Preconditions.checkNotNull(instituteName);
    return NAME_PREFIX.concat(instituteName);
  }

  @VisibleForTesting
  String composeDescription(String instituteName) {
    Preconditions.checkNotNull(instituteName);
    return DESCRIPTION_PREFIX.concat(instituteName);
  }
}

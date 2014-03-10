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
package nl.surfnet.bod.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.sab.EntitlementsHandler;

import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import com.google.common.base.Preconditions;

@Service("sabGroupService")
public class SabGroupService implements GroupService {

  public static final String GROUP_PREFIX = "urn:collab:person:sab:admin:bod.surfnet.nl:";

  static final String NAME_PREFIX = "BoD Administrator ";
  static final String DESCRIPTION_PREFIX = NAME_PREFIX + " of ";

  @Resource private EntitlementsHandler entitlementsHandler;

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
    return GROUP_PREFIX.concat(instituteName);
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

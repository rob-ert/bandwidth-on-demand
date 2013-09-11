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
package nl.surfnet.bod.util;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.view.ElementActionView;
import nl.surfnet.bod.web.view.PhysicalPortView;
import nl.surfnet.bod.web.view.UserGroupView;

public final class Functions {

  private Functions() {
  }

  public static final Function<UserGroup, UserGroupView> FROM_USER_GROUP_TO_USER_GROUP_VIEW =
      new Function<UserGroup, UserGroupView>() {
        @Override
        public UserGroupView apply(UserGroup group) {
          return new UserGroupView(group);
        }
      };

  public static final Function<VirtualResourceGroup, UserGroupView> FROM_VRG_TO_USER_GROUP_VIEW =
      new Function<VirtualResourceGroup, UserGroupView>() {
        @Override
        public UserGroupView apply(VirtualResourceGroup group) {
          return new UserGroupView(group);
        }
      };

  /**
   * Calculates the amount of related {@link VirtualPort}s and transforms it to a {@link PhysicalPortView}
   */
  public static PhysicalPortView transformAllocatedPhysicalPort(UniPort port,
      VirtualPortService virtualPortService, ReservationService reservationService) {

    long vpCount = virtualPortService.countForUniPort(port);
    ElementActionView allocateActionView;
    if (vpCount == 0) {
      allocateActionView = new ElementActionView(true, "label_unallocate");
    } else {
      allocateActionView = new ElementActionView(false, "label_virtual_ports_related");
    }

    PhysicalPortView physicalPortView = new PhysicalPortView(port, allocateActionView, vpCount);
    physicalPortView.setReservationsAmount(reservationService.findActiveByPhysicalPort(port).size());

    return physicalPortView;
  }

  /**
   * Calculates the amount of related {@link VirtualPort}s and transforms it to a {@link PhysicalPortView}
   */
  public static PhysicalPortView transformAllocatedPhysicalPort(EnniPort port, ReservationService reservationService, NsiHelper nsiHelper) {

    ElementActionView allocateActionView = new ElementActionView(false, "label_virtual_ports_related");

    PhysicalPortView physicalPortView = new PhysicalPortView(port, allocateActionView, nsiHelper);
    physicalPortView.setReservationsAmount(reservationService.findActiveByPhysicalPort(port).size());

    return physicalPortView;
  }

  public static List<PhysicalPortView> transformAllocatedPhysicalPorts(List<? extends EnniPort> ports, ReservationService reservationService, NsiHelper nsiHelper) {
    List<PhysicalPortView> views = Lists.newArrayList();
    for (EnniPort port : ports) {
      views.add(transformAllocatedPhysicalPort(port, reservationService, nsiHelper));
    }

    return views;
  }

  public static List<PhysicalPortView> transformAllocatedPhysicalPorts(List<? extends UniPort> ports, VirtualPortService virtualPortService, ReservationService reservationService) {
    List<PhysicalPortView> views = Lists.newArrayList();
    for (UniPort port : ports) {
      views.add(transformAllocatedPhysicalPort(port, virtualPortService, reservationService));
    }

    return views;
  }

  public static List<PhysicalPortView> transformUnalignedPhysicalPorts(
      List<? extends PhysicalPort> ports,
      VirtualPortService virtualPortService,
      ReservationService reservationService,
      NsiHelper nsiHelper) {

    List<PhysicalPortView> views = Lists.newArrayList();
    for (PhysicalPort port : ports) {
      if (port instanceof UniPort) {
        views.add(transformAllocatedPhysicalPort((UniPort) port, virtualPortService, reservationService));
      } else if (port instanceof EnniPort) {
        views.add(transformAllocatedPhysicalPort((EnniPort) port,  reservationService, nsiHelper));
      } else {
        throw new IllegalArgumentException("Don't know how to handle an instance of " + port.getClass());
      }
    }

    return views;
  }

  public static PhysicalPortView transformUnallocatedPhysicalPort(NbiPort unallocatedPort) {
    return new PhysicalPortView(unallocatedPort);
  }

  public static List<PhysicalPortView> transformUnallocatedPhysicalPorts(Collection<NbiPort> unallocatedPorts) {
    List<PhysicalPortView> transformers = Lists.newArrayList();
    for (NbiPort port : unallocatedPorts) {
      transformers.add(transformUnallocatedPhysicalPort(port));
    }

    return transformers;
  }

}

package nl.surfnet.bod.util;

import java.util.ArrayList;
import java.util.List;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.view.ElementActionView;
import nl.surfnet.bod.web.view.PhysicalPortView;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public final class Functions {

  private Functions() {

  }

  /**
   * Calculates the amount of related {@link VirtualPort}s and transforms it to
   * a {@link PhysicalPortView}
   * 
   * @param port
   *          {@link PhysicalPort} to enrich
   * @param virtualPortService
   *          {@link VirtualPortService} to retrieve the amount of related
   *          {@link VirtualPort}s
   * @return PhysicalPortView Transformed {@link PhysicalPort}
   */
  public static PhysicalPortView transformPhysicalPort(final PhysicalPort port,
      final VirtualPortService virtualPortService) {

    long vpCount = virtualPortService.countForPhysicalPort(port);
    ElementActionView allocateActionView;
    if (vpCount == 0) {
      allocateActionView = new ElementActionView(true, "label_unallocate");
    }
    else {
      allocateActionView = new ElementActionView(false, "label_virtual_ports_related");
    }

    return new PhysicalPortView(port, allocateActionView, vpCount);
  }

  /**
   * Transforms a Collection
   * 
   * @see #transformPhysicalPort(PhysicalPort, VirtualPortService)
   * 
   */
  public static List<PhysicalPortView> transformPhysicalPorts(final List<PhysicalPort> ports,
      final VirtualPortService virtualPortService) {

    List<PhysicalPortView> transformers = new ArrayList<PhysicalPortView>();
    for (PhysicalPort port : ports) {
      transformers.add(transformPhysicalPort(port, virtualPortService));
    }

    return transformers;
  }

  /**
   * Enriches the given {@link PhysicalPort} with the related {@link Institute}
   * and transforms it to a {@link PhysicalPortView}
   * 
   * @param port
   *          {@link PhysicalPort} to enrich
   * @param instituteService
   *          {@link InstituteService} to retrieve the related {@link Institute}
   * @param ElementActionView
   *          view to determine if unallocate is allowed
   * 
   * @return PhysicalPortView Transformed and enriched {@link PhysicalPort}
   */
  public static PhysicalPortView enrichAndTransformPhysicalPort(final PhysicalPort port,
      final InstituteService instituteService, final ElementActionView unallocateActionView) {

    if (port.getPhysicalResourceGroup() != null) {
      instituteService.fillInstituteForPhysicalResourceGroup(port.getPhysicalResourceGroup());
    }

    return new PhysicalPortView(port, unallocateActionView);
  }

  /**
   * Enriches and transforms a collection. The
   * {@link ElementActionView#isAllowed()} will be set to false.
   * 
   * @see #enrichAndTransformPhysicalPort(PhysicalPort, InstituteService)
   */
  public static List<PhysicalPortView> enrichAndTransformUnallocatedPhysicalPort(final List<PhysicalPort> ports,
      final InstituteService instituteService) {

    List<PhysicalPortView> transformers = new ArrayList<PhysicalPortView>();
    for (PhysicalPort port : ports) {
      transformers.add(enrichAndTransformPhysicalPort(port, instituteService, null));
    }

    return transformers;
  }

  /**
   * Enriches and transforms a collection. The
   * {@link ElementActionView#isAllowed()} will depend if there are
   * {@link VirtualPort}s related to a given port.
   * 
   */
  public static List<PhysicalPortView> enrichAndTransformAllocatedPhysicalPort(final List<PhysicalPort> ports,
      final InstituteService instituteService, final VirtualPortService virtualPortService) {

    List<PhysicalPortView> transformers = new ArrayList<PhysicalPortView>();
    for (PhysicalPort port : ports) {

      ElementActionView unallocateActionView;
      if (virtualPortService.countForPhysicalPort(port) == 0) {
        unallocateActionView = new ElementActionView(true, "label_unallocate");
      }
      else {
        unallocateActionView = new ElementActionView(false, "label_virtual_ports_related");
      }

      transformers.add(enrichAndTransformPhysicalPort(port, instituteService, unallocateActionView));
    }

    return transformers;
  }

  public static final Function<PhysicalPort, String> TO_NETWORK_ELEMENT_PK = //
  new Function<PhysicalPort, String>() {

    @Override
    public String apply(PhysicalPort physicalPort) {
      return physicalPort.getNetworkElementPk();
    }
  };

  public static final Predicate<PhysicalPort> MISSING_PORTS = //
  new Predicate<PhysicalPort>() {
    @Override
    public boolean apply(PhysicalPort physicalPort) {
      return physicalPort.isMissing();
    }
  };

  public static final Predicate<PhysicalPort> NON_MISSING_PORTS = //
  new Predicate<PhysicalPort>() {
    @Override
    public boolean apply(PhysicalPort physicalPort) {
      return !MISSING_PORTS.apply(physicalPort);
    }
  };

}

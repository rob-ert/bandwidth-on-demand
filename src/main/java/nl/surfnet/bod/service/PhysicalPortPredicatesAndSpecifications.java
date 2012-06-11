package nl.surfnet.bod.service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalPort_;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Predicate;

public final class PhysicalPortPredicatesAndSpecifications {

  private PhysicalPortPredicatesAndSpecifications() {
  }

  public static final Specification<PhysicalPort> MISSING_PORT_SPEC = new Specification<PhysicalPort>() {

    @Override
    public javax.persistence.criteria.Predicate toPredicate(Root<PhysicalPort> physicalPort, CriteriaQuery<?> query,
        CriteriaBuilder cb) {
      return cb.equal(physicalPort.get(PhysicalPort_.missing), true);
    }
  };

  public static Specification<PhysicalPort> BY_PHYSICAL_RESOURCE_GROUP_SPEC(
      final PhysicalResourceGroup physicalResourceGroup) {
    return new Specification<PhysicalPort>() {

      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<PhysicalPort> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {

        return cb.equal(root.get(PhysicalPort_.physicalResourceGroup), physicalResourceGroup);
      }
    };
  }

  public static Predicate<PhysicalPort> UNALLOCATED_PORTS_PRED = new Predicate<PhysicalPort>() {
    @Override
    public boolean apply(PhysicalPort input) {
      return input.getId() == null;
    }
  };
}

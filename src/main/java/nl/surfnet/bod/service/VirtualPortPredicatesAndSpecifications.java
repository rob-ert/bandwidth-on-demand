package nl.surfnet.bod.service;

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalPort_;
import nl.surfnet.bod.domain.PhysicalResourceGroup_;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.domain.VirtualPortRequestLink_;
import nl.surfnet.bod.domain.VirtualPort_;
import nl.surfnet.bod.domain.VirtualResourceGroup_;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.joda.time.DateMidnight;
import org.joda.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class VirtualPortPredicatesAndSpecifications {

  private VirtualPortPredicatesAndSpecifications() {
  }

  static final Specification<VirtualPort> FOR_USER_SPEC(final RichUserDetails user) {
    return new Specification<VirtualPort>() {
      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<VirtualPort> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {
        return cb.and(root.get(VirtualPort_.virtualResourceGroup).get(VirtualResourceGroup_.surfconextGroupId)
            .in(user.getUserGroupIds()));
      }
    };
  }

  static final Specification<VirtualPort> FOR_MANAGER_SPEC(final BodRole managerRole) {
    return new Specification<VirtualPort>() {

      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<VirtualPort> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {
        return cb
            .equal(
                root.get(VirtualPort_.physicalPort).get(PhysicalPort_.physicalResourceGroup)
                    .get(PhysicalResourceGroup_.id), managerRole.getPhysicalResourceGroupId());
      }
    };
  }

  static final Specification<VirtualPort> BY_PHYSICAL_PORT_SPEC(final PhysicalPort physicalPort) {
    return new Specification<VirtualPort>() {

      private Long physicalPortId = physicalPort.getId();

      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<VirtualPort> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {
        return cb.and(cb.equal(root.get(VirtualPort_.physicalPort).get(PhysicalPort_.id), physicalPortId));
      }
    };
  }

  static final Specification<VirtualPortRequestLink> BY_GROUP_ID_IN_LAST_MONTH_SPEC(final Collection<String> vrgUrns) {
    return new Specification<VirtualPortRequestLink>() {

      @Override
      public Predicate toPredicate(Root<VirtualPortRequestLink> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        LocalDateTime start = new LocalDateTime(DateMidnight.now().plusDays(1).toDate().getTime());

        return cb
            .and(
                cb.between(root.get(VirtualPortRequestLink_.requestDateTime), start.minusDays(31), start),
                root.get(VirtualPortRequestLink_.virtualResourceGroup).get(VirtualResourceGroup_.surfconextGroupId)
                    .in(vrgUrns));
      }
    };
  }

}

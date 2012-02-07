package nl.surfnet.bod.repo;

import nl.surfnet.bod.domain.ActivationEmailLink;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivationEmailLinkRepo extends JpaSpecificationExecutor<ActivationEmailLink<?>>,
    JpaRepository<ActivationEmailLink<?>, Long> {

  ActivationEmailLink<?> findByUuid(String uuid);
}

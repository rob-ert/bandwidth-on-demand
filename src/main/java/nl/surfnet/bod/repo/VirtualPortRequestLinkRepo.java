package nl.surfnet.bod.repo;

import nl.surfnet.bod.domain.VirtualPortRequestLink;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualPortRequestLinkRepo extends JpaSpecificationExecutor<VirtualPortRequestLink>,
    JpaRepository<VirtualPortRequestLink, Long> {

  VirtualPortRequestLink findByUuid(String uuid);
}

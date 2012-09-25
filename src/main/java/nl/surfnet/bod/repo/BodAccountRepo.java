package nl.surfnet.bod.repo;

import nl.surfnet.bod.domain.BodAccount;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BodAccountRepo extends JpaRepository<BodAccount, Long> {

  BodAccount findByNameId(String nameId);
}

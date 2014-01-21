package nl.surfnet.bod.nsi.v2;

import java.util.List;

import com.google.common.base.Optional;

public interface NsiV2MessageRepoCustom {

  List<NsiV2Message> findQueryResults(String connectionId, Optional<Long> resultIdStart, Optional<Long> resultIdEnd);
}
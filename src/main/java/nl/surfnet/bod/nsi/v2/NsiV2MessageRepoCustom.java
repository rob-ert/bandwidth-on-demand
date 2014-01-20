package nl.surfnet.bod.nsi.v2;

import java.util.List;

public interface NsiV2MessageRepoCustom {

  List<NsiV2Message> findQueryResults(String connectionId, Long resultIdStart, Long resultIdEnd);
}
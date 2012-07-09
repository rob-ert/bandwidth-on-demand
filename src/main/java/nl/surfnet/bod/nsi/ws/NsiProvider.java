package nl.surfnet.bod.nsi.ws;

import nl.surfnet.bod.domain.Connection;

import org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort;

public interface NsiProvider extends ConnectionProviderPort {

  void reserveConfirmed(Connection connection);

  void reserveFailed(Connection connection);
}

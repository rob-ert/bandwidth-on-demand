package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.Connection;

import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;

public class ConnectionFactory {

  private int desiredBandwidth;
  private String requesterNSA = "nsa:requester:surfnet.nl";
  private String providerNSA = "nsa:surfnet.nl";
  private String connectionId = "2345-567hj-678hj";
  private String sourceStpId = "source port";
  private String destinationStpId = "destination port";

  public Connection create() {
    Connection connection = new Connection();

    connection.setDesiredBandwidth(desiredBandwidth);
    connection.setRequesterNsa(requesterNSA);
    connection.setProviderNsa(providerNSA);
    connection.setConnectionId(connectionId);
    connection.setSourceStpId(sourceStpId);
    connection.setDestinationStpId(destinationStpId);

    ServiceTerminationPointType sourceStp = new ServiceTerminationPointType();
    sourceStp.setStpId(sourceStpId);
    ServiceTerminationPointType dstStp = new ServiceTerminationPointType();
    dstStp.setStpId(destinationStpId);

    PathType pathType = new PathType();
    pathType.setDestSTP(dstStp);
    pathType.setSourceSTP(sourceStp);
    connection.setPath(pathType);

    return connection;
  }

  public ConnectionFactory setDestinationStpId(String destinationStpId) {
    this.destinationStpId = destinationStpId;
    return this;
  }

  public ConnectionFactory setSourceStpId(String sourceStpId) {
    this.sourceStpId = sourceStpId;
    return this;
  }

  public ConnectionFactory setConnectionId(String connectionId) {
    this.connectionId = connectionId;
    return this;
  }

  public ConnectionFactory setProviderNSA(String providerNSA) {
    this.providerNSA = providerNSA;
    return this;
  }

  public ConnectionFactory setRequesterNSA(String requesterNSA) {
    this.requesterNSA = requesterNSA;
    return this;
  }

  public ConnectionFactory setDesiredBandwidth(int desiredBandwidth) {
    this.desiredBandwidth = desiredBandwidth;
    return this;
  }
}

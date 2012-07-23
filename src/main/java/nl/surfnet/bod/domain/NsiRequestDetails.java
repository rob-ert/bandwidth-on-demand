package nl.surfnet.bod.domain;

public class NsiRequestDetails {
  private final String replyTo;
  private final String correlationId;

  public NsiRequestDetails(String replyTo, String correlationId) {
    this.replyTo = replyTo;
    this.correlationId = correlationId;
  }

  public String getReplyTo() {
    return replyTo;
  }

  public String getCorrelationId() {
    return correlationId;
  }

}
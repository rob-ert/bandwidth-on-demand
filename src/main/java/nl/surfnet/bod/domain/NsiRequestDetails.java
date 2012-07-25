package nl.surfnet.bod.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class NsiRequestDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String replyTo;
  private String correlationId;

  private NsiRequestDetails() {
  }

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

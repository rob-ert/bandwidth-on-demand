package nl.surfnet.bod.domain.oauth;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessToken {

  private Long id;
  private String token;
  private long expires;
  private List<String> scopes;
  private String resourceOwnerId;
  private Client client;

  public String getToken() {
    return token;
  }

  public long getExpires() {
    return expires;
  }

  public List<String> getScopes() {
    return scopes;
  }

  public String getResourceOwnerId() {
    return resourceOwnerId;
  }

  public Long getId() {
    return id;
  }

  public Client getClient() {
    return client;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public class Client {
    private String clientId;

    public String getClientId() {
      return clientId;
    }
  }

}

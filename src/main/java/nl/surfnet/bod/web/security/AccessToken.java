package nl.surfnet.bod.web.security;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"encodedPrincipal"})
public class AccessToken {

  private Long id;
  private Date creationDate;
  private Date modificationDate;
  private long expires;
  private String token;
  private String refreshToken;
  private List<String> scopes;
  private String resourceOwnerId;
  private Client client;
  private Principal principal;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public List<String> getScopes() {
    return scopes;
  }

  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  public String getResourceOwnerId() {
    return resourceOwnerId;
  }

  public void setResourceOwnerId(String resourceOwnerId) {
    this.resourceOwnerId = resourceOwnerId;
  }

  public long getExpires() {
    return expires;
  }

  public void setExpires(long expires) {
    this.expires = expires;
  }

  public Client getClient() {
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }


  public Principal getPrincipal() {
    return principal;
  }

  public void setPrincipal(Principal principal) {
    this.principal = principal;
  }

  @JsonIgnoreProperties({"thumbNailUrl", "attributes", "contactName", "contactEmail"})
  public static class Client {
    private Long id;
    private Date creationDate;
    private Date modificationDate;
    private String name;
    private String clientId;
    private String secret;
    private String description;
    private List<String> scopes;
    private List<String> redirectUris;
    private boolean skipConsent;
    private long expireDuration;
    private boolean useRefreshTokens;
    private boolean notAllowedImplicitGrant;

    public Long getId() {
      return id;
    }
    public void setId(Long id) {
      this.id = id;
    }
    public Date getCreationDate() {
      return creationDate;
    }
    public void setCreationDate(Date creationDate) {
      this.creationDate = creationDate;
    }
    public Date getModificationDate() {
      return modificationDate;
    }
    public void setModificationDate(Date modificationDate) {
      this.modificationDate = modificationDate;
    }
    public String getName() {
      return name;
    }
    public void setName(String name) {
      this.name = name;
    }
    public String getClientId() {
      return clientId;
    }
    public void setClientId(String clientId) {
      this.clientId = clientId;
    }
    public String getSecret() {
      return secret;
    }
    public void setSecret(String secret) {
      this.secret = secret;
    }
    public String getDescription() {
      return description;
    }
    public void setDescription(String description) {
      this.description = description;
    }
    public List<String> getScopes() {
      return scopes;
    }
    public void setScopes(List<String> scopes) {
      this.scopes = scopes;
    }
    public List<String> getRedirectUris() {
      return redirectUris;
    }
    public void setRedirectUris(List<String> redirectUris) {
      this.redirectUris = redirectUris;
    }
    public boolean isSkipConsent() {
      return skipConsent;
    }
    public void setSkipConsent(boolean skipConsent) {
      this.skipConsent = skipConsent;
    }
    public long getExpireDuration() {
      return expireDuration;
    }
    public void setExpireDuration(long expireDuration) {
      this.expireDuration = expireDuration;
    }
    public boolean isUseRefreshTokens() {
      return useRefreshTokens;
    }
    public void setUseRefreshTokens(boolean useRefreshTokens) {
      this.useRefreshTokens = useRefreshTokens;
    }
    public boolean isNotAllowedImplicitGrant() {
      return notAllowedImplicitGrant;
    }
    public void setNotAllowedImplicitGrant(boolean notAllowImplicitGrant) {
      this.notAllowedImplicitGrant = notAllowImplicitGrant;
    }
  }

  public static class Principal {
    private String name;
    private Collection<String> roles;
    private Map<String, Object> attributes;

    public String getName() {
      return name;
    }
    public void setName(String name) {
      this.name = name;
    }
    public Collection<String> getRoles() {
      return roles;
    }
    public void setRoles(Collection<String> roles) {
      this.roles = roles;
    }
    public Map<String, Object> getAttributes() {
      return attributes;
    }
    public void setAttributes(Map<String, Object> attributes) {
      this.attributes = attributes;
    }
  }
}

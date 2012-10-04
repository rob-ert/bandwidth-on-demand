package nl.surfnet.bod.domain;

import javax.persistence.*;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

@Entity
public class BodAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  
  @Column(unique = true, nullable = false)
  private String nameId;
  
  private String authorizationServerAccessToken;
  private String accessToken;
  
  @Version
  private long version;

  public String getNameId() {
    return nameId;
  }
  public void setNameId(String nameId) {
    this.nameId = nameId;
  }
  public Optional<String> getAuthorizationServerAccessToken() {
    return Optional.fromNullable(Strings.emptyToNull(authorizationServerAccessToken));
  }
  public void setAuthorizationServerAccessToken(String authorizationServerAccessToken) {
    this.authorizationServerAccessToken = authorizationServerAccessToken;
  }
  public Optional<String> getAccessToken() {
    return Optional.fromNullable(Strings.emptyToNull(accessToken));
  }
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public long getVersion() {
    return version;
  }
  public void setVersion(long version) {
    this.version = version;
  }
  public void removeAccessToken() {
    this.accessToken = null;
  }
}

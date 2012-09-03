/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.domain;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import nl.surfnet.bod.web.security.Security.RoleEnum;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Represents a customer of SURFnet, a local copy of IDD data, which is regarded
 * by the rest of the team as a good idea...
 * 
 * Note that this class has <strong>no version</strong> field, since this causes
 * duplicate key exceptions because hibernate tries to insert them instead of
 * updating the existing content of the database.
 * 
 */
@Indexed
@Entity
public class Institute implements Loggable {

  @Id
  private Long id;

  @NotEmpty
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private String name;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @NotEmpty
  private String shortName;

  @Basic
  @Column(name = "aligned_idd")
  private boolean alignedWithIDD;

  // No version!
  @SuppressWarnings("unused")
  private Institute() {
  }

  public Institute(Long id, String name, String shortName, boolean alignedWithIDD) {
    this.id = id;
    this.name = name;
    this.shortName = shortName;
    this.alignedWithIDD = alignedWithIDD;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getShortName() {
    return shortName;
  }

  public boolean isAlignedWithIDD() {
    return alignedWithIDD;
  }

  public void setAlignedWithIDD(boolean alignedWithIDD) {
    this.alignedWithIDD = alignedWithIDD;
  }

  @Override
  public String getAdminGroup() {
    return RoleEnum.NOC_ENGINEER.name();
  }
  
  @Override
  public String getLabel() {
   return getShortName();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Institute [");
    if (id != null) {
      builder.append("id=");
      builder.append(id);
      builder.append(", ");
    }
    if (name != null) {
      builder.append("name=");
      builder.append(name);
      builder.append(", ");
    }
    if (shortName != null) {
      builder.append("shortName=");
      builder.append(shortName);
      builder.append(", ");
    }
    builder.append("alignedWithIDD=");
    builder.append(alignedWithIDD);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((shortName == null) ? 0 : shortName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Institute other = (Institute) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    if (shortName == null) {
      if (other.shortName != null)
        return false;
    }
    else if (!shortName.equals(other.shortName))
      return false;
    return true;
  }

}

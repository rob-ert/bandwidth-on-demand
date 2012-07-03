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

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Represents a customer of SURFnet, a local copy of IDD data, which is regarded
 * by the rest of the team as a good idea...
 * 
 * Note that this class has <strong>no version</strong> field, since this causes
 * duplicate key exceptions because hibernate tries to insert them instead of
 * updating the existing content of the database.
 * 
 * @author Franky
 * 
 */
@Entity
public class Institute {

  @Id
  private Long id;
  @NotEmpty
  private String name;
  @NotEmpty
  private String shortName;
  @Basic
  @Column(name = "aligned_idd")
  private boolean alignedWithIDD;

  // No version!

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

}

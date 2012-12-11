/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.domain;

import java.util.Collection;
import java.util.Collections;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
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
@Analyzer(definition = "customanalyzer")
public class Institute implements Loggable {

  @Id
  @DocumentId
  private Long id;

  @Field
  @NotEmpty
  private String name;

  @Field
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

  @Override
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
  public Collection<String> getAdminGroups() {
    return Collections.<String>emptyList();
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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Institute other = (Institute) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    }
    else if (!id.equals(other.id)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    }
    else if (!name.equals(other.name)) {
      return false;
    }
    if (shortName == null) {
      if (other.shortName != null) {
        return false;
      }
    }
    else if (!shortName.equals(other.shortName)) {
      return false;
    }
    return true;
  }

}

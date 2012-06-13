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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Represents a customer of SURFnet, a local copy of IDD data, which is regarded
 * by the rest of the team as a good idea...
 * 
 * @author Franky
 * 
 */
public class Institute {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private final Long id;
  @NotEmpty
  private final String name;
  @NotEmpty
  private final String shortName;
  @Version
  private Integer version;

  public Institute(Long id, String name, String shortName) {
    this.id = id;
    this.name = name;
    this.shortName = shortName;
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

  public Integer getVersion() {
    return version;
  }

}

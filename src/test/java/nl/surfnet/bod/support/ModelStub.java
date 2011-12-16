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
package nl.surfnet.bod.support;

import java.util.Collection;
import java.util.Map;

import org.springframework.ui.Model;

import com.google.common.collect.Maps;

public class ModelStub implements Model {

  private final Map<String, Object> attributes = Maps.newHashMap();

  @Override
  public Model addAttribute(String attributeName, Object attributeValue) {
    attributes.put(attributeName, attributeValue);
    return this;
  }

  @Override
  public Model addAttribute(Object attributeValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Model addAllAttributes(Collection<?> attributeValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Model addAllAttributes(Map<String, ?> attr) {
    this.attributes.putAll(attr);
    return this;
  }

  @Override
  public Model mergeAttributes(Map<String, ?> attributes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAttribute(String attributeName) {
    return attributes.containsKey(attributeName);
  }

  @Override
  public Map<String, Object> asMap() {
    return attributes;
  }
}

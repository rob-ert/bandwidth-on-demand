/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.support;

import java.util.Collection;
import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Maps;

public class ModelStub implements RedirectAttributes {

  private final Map<String, Object> attributes = Maps.newHashMap();
  private final Map<String, Object> flashAttributes = Maps.newHashMap();

  @Override
  public RedirectAttributes addAttribute(String attributeName, Object attributeValue) {
    attributes.put(attributeName, attributeValue);
    return this;
  }

  @Override
  public RedirectAttributes addAttribute(Object attributeValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RedirectAttributes addAllAttributes(Collection<?> attributeValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Model addAllAttributes(Map<String, ?> attr) {
    this.attributes.putAll(attr);
    return this;
  }

  @Override
  public RedirectAttributes mergeAttributes(Map<String, ?> attributes) {
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

  @Override
  public RedirectAttributes addFlashAttribute(String attributeName, Object attributeValue) {
    flashAttributes.put(attributeName, attributeValue);
    return this;
  }

  @Override
  public RedirectAttributes addFlashAttribute(Object attributeValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, ?> getFlashAttributes() {
    return flashAttributes;
  }
}

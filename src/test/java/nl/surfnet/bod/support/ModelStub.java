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

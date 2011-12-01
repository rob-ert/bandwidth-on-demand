package nl.surfnet.bod.web;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

/**
 * A central place to register application converters and formatters.
 */
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

  @Autowired
  private PhysicalPortService physicalPortService;

  @Autowired
  private PhysicalResourceGroupServiceImpl physicalResourceGroupService;

  @Override
  protected void installFormatters(final FormatterRegistry registry) {
    super.installFormatters(registry);
    // Register application converters and formatters
  }

  public Converter<PhysicalPort, String> getPhysicalPortToStringConverter() {
    return new Converter<PhysicalPort, String>() {
      @Override
      public String convert(final PhysicalPort physicalPort) {
        return new StringBuilder().append(physicalPort.getName()).toString();
      }
    };
  }

  public Converter<Long, PhysicalPort> getIdToPhysicalPortConverter() {
    return new Converter<Long, PhysicalPort>() {
      @Override
      public PhysicalPort convert(final java.lang.Long id) {
        return physicalPortService.find(id);
      }
    };
  }

  public Converter<String, PhysicalPort> getStringToPhysicalPortConverter() {
    return new Converter<java.lang.String, nl.surfnet.bod.domain.PhysicalPort>() {
      @Override
      public PhysicalPort convert(final String id) {
        return getObject().convert(getObject().convert(id, Long.class), PhysicalPort.class);
      }
    };
  }

  public Converter<PhysicalResourceGroup, String> getPhysicalResourceGroupToStringConverter() {
    return new Converter<PhysicalResourceGroup, String>() {
      @Override
      public String convert(final PhysicalResourceGroup physicalResourceGroup) {
        return new StringBuilder().append(physicalResourceGroup.getName()).append(" - ")
            .append(physicalResourceGroup.getInstitutionName()).toString();
      }
    };
  }

  public Converter<Long, PhysicalResourceGroup> getIdToPhysicalResourceGroupConverter() {
    return new Converter<Long, PhysicalResourceGroup>() {
      @Override
      public PhysicalResourceGroup convert(final java.lang.Long id) {
        return physicalResourceGroupService.find(id);
      }
    };
  }

  public Converter<String, PhysicalResourceGroup> getStringToPhysicalResourceGroupConverter() {
    return new Converter<java.lang.String, PhysicalResourceGroup>() {
      @Override
      public PhysicalResourceGroup convert(final String id) {
        return getObject().convert(getObject().convert(id, Long.class), PhysicalResourceGroup.class);
      }
    };
  }

  public void installLabelConverters(final FormatterRegistry registry) {
    registry.addConverter(getPhysicalPortToStringConverter());
    registry.addConverter(getIdToPhysicalPortConverter());
    registry.addConverter(getStringToPhysicalPortConverter());
    registry.addConverter(getPhysicalResourceGroupToStringConverter());
    registry.addConverter(getIdToPhysicalResourceGroupConverter());
    registry.addConverter(getStringToPhysicalResourceGroupConverter());
  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    installLabelConverters(getObject());
  }
}

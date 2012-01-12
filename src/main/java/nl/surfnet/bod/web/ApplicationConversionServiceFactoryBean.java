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
package nl.surfnet.bod.web;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.number.NumberFormatAnnotationFormatterFactory;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

/**
 * A central place to register application converters and formatters.
 */
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

  @Autowired
  private PhysicalPortService physicalPortService;

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Autowired
  private VirtualResourceGroupService virtualResourceGroupService;

  @Autowired
  private VirtualPortService virtualPortService;

  @Override
  protected void installFormatters(FormatterRegistry registry) {
    registry.addFormatterForFieldAnnotation(new NumberFormatAnnotationFormatterFactory());
    new JodaTimeFormattingPatternConfigurer().setDateTimePattern("yyyy-MM-dd H:mm").setTimePattern("H:mm")
        .setDatePattern("yyyy-MM-dd").installJodaTimeFormatting(registry);
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
      public PhysicalPort convert(final Long id) {
        return physicalPortService.find(id);
      }
    };
  }

  public Converter<String, PhysicalPort> getStringToPhysicalPortConverter() {
    return new Converter<String, PhysicalPort>() {
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
        return new StringBuilder()
            .append(physicalResourceGroup.getName())
            .append(" - ")
            .append(
                physicalResourceGroup.getInstitute() == null ? null : physicalResourceGroup.getInstitute().getName())
            .toString();
      }
    };
  }

  public Converter<Long, PhysicalResourceGroup> getIdToPhysicalResourceGroupConverter() {
    return new Converter<Long, PhysicalResourceGroup>() {
      @Override
      public PhysicalResourceGroup convert(final Long id) {
        return physicalResourceGroupService.find(id);
      }
    };
  }

  public Converter<String, PhysicalResourceGroup> getStringToPhysicalResourceGroupConverter() {
    return new Converter<String, PhysicalResourceGroup>() {
      @Override
      public PhysicalResourceGroup convert(final String id) {
        return getObject().convert(getObject().convert(id, Long.class), PhysicalResourceGroup.class);
      }
    };
  }

  public Converter<String, VirtualResourceGroup> getStringToVirtualResourceGroupConverter() {
    return new Converter<String, VirtualResourceGroup>() {
      @Override
      public VirtualResourceGroup convert(final String id) {
        return getObject().convert(getObject().convert(id, Long.class), VirtualResourceGroup.class);
      }
    };
  }

  public Converter<Long, VirtualResourceGroup> getIdToVirtualResourceGroupConverter() {
    return new Converter<Long, VirtualResourceGroup>() {
      @Override
      public VirtualResourceGroup convert(Long id) {
        return virtualResourceGroupService.find(id);
      }
    };
  }

  public Converter<VirtualResourceGroup, String> getVirtualResourceGroupToStringConverter() {
    return new Converter<VirtualResourceGroup, String>() {
      @Override
      public String convert(final VirtualResourceGroup virtualResourceGroup) {
        return virtualResourceGroup.getName();
      }
    };
  }

  public Converter<String, VirtualPort> getStringToVirtualPortConverter() {
    return new Converter<String, VirtualPort>() {
      @Override
      public VirtualPort convert(final String id) {
        return getObject().convert(getObject().convert(id, Long.class), VirtualPort.class);
      }
    };
  }

  public Converter<Long, VirtualPort> getIdToVirtualPortConverter() {
    return new Converter<Long, VirtualPort>() {
      @Override
      public VirtualPort convert(Long id) {
        return virtualPortService.find(Long.valueOf(id));
      }
    };
  }

  public Converter<VirtualPort, String> getVirtualPortToStringConverter() {
    return new Converter<VirtualPort, String>() {
      @Override
      public String convert(VirtualPort virtualPort) {
        return virtualPort.getName();
      }
    };
  }

  public void installLabelConverters(final FormatterRegistry registry) {
    // physical ports
    registry.addConverter(getPhysicalPortToStringConverter());
    registry.addConverter(getStringToPhysicalPortConverter());
    registry.addConverter(getIdToPhysicalPortConverter());

    // physical resource groups
    registry.addConverter(getPhysicalResourceGroupToStringConverter());
    registry.addConverter(getStringToPhysicalResourceGroupConverter());
    registry.addConverter(getIdToPhysicalResourceGroupConverter());

    // virtual resource groups
    registry.addConverter(getVirtualResourceGroupToStringConverter());
    registry.addConverter(getStringToVirtualResourceGroupConverter());
    registry.addConverter(getIdToVirtualResourceGroupConverter());

    // virtual ports
    registry.addConverter(getVirtualPortToStringConverter());
    registry.addConverter(getStringToVirtualPortConverter());
    registry.addConverter(getIdToVirtualPortConverter());
  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    installLabelConverters(getObject());
  }
}

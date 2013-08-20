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
package nl.surfnet.bod.web.base;

import javax.annotation.Resource;
import javax.xml.datatype.XMLGregorianCalendar;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.ReservationEndPoint;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;

import org.joda.time.DateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.number.NumberFormatAnnotationFormatterFactory;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

/**
 * A central place to register application converters and formatters.
 */
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

  @Resource
  private PhysicalPortService physicalPortService;

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Resource
  private VirtualResourceGroupService virtualResourceGroupService;

  @Resource
  private VirtualPortService virtualPortService;

  public Converter<XMLGregorianCalendar, String> getXmlGregorianCalendarToStringConverter() {
    return new Converter<XMLGregorianCalendar, String>() {
      @Override
      public String convert(XMLGregorianCalendar calendar) {
        return new DateTime(calendar.toGregorianCalendar()).toString("yyy-MM-dd H:mm");
      }
    };
  }

  public Converter<UniPort, String> getPhysicalPortToStringConverter() {
    return new Converter<UniPort, String>() {
      @Override
      public String convert(final UniPort physicalPort) {
        return physicalPort.getNocLabel();
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

  public Converter<Long, VirtualPortRequestLink> getIdToVirtualPortRequestLinkConverter() {
    return new Converter<Long, VirtualPortRequestLink>() {
      @Override
      public VirtualPortRequestLink convert(final Long id) {
        return virtualPortService.findRequest(id);
      }
    };
  }

  public Converter<String, UniPort> getStringToPhysicalPortConverter() {
    return new Converter<String, UniPort>() {
      @Override
      public UniPort convert(final String id) {
        return getObject().convert(getObject().convert(id, Long.class), UniPort.class);
      }
    };
  }

  public Converter<PhysicalResourceGroup, String> getPhysicalResourceGroupToStringConverter() {
    return new Converter<PhysicalResourceGroup, String>() {
      @Override
      public String convert(final PhysicalResourceGroup physicalResourceGroup) {
        return physicalResourceGroup.getName();
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

  public Converter<String, VirtualPortRequestLink> getStringToVirtualPortRequestLinkConverter() {
    return new Converter<String, VirtualPortRequestLink>() {
      @Override
      public VirtualPortRequestLink convert(String id) {
        return getObject().convert(getObject().convert(id, Long.class), VirtualPortRequestLink.class);
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
        return virtualPortService.find(id);
      }
    };
  }

  public Converter<String, ReservationEndPoint> getVirtualPortIdAsStringToReservationEndPointConverter() {
    return new Converter<String, ReservationEndPoint>() {
      @Override
      public ReservationEndPoint convert(final String id) {
        return getObject().convert(getObject().convert(id, Long.class), ReservationEndPoint.class);
      }
    };
  }

  public Converter<Long, ReservationEndPoint> getVirtualPortIdToReservationEndPointConverter() {
    return new Converter<Long, ReservationEndPoint>() {
      @Override
      public ReservationEndPoint convert(Long id) {
        VirtualPort virtualPort = virtualPortService.find(id);
        return virtualPort == null ? null : new ReservationEndPoint(virtualPort);
      }
    };
  }

  public Converter<VirtualPort, String> getVirtualPortToStringConverter() {
    return new Converter<VirtualPort, String>() {
      @Override
      public String convert(VirtualPort virtualPort) {
        return virtualPort.getManagerLabel();
      }
    };
  }

  public Converter<Institute, String> getInstituteToStringConverter() {
    return new Converter<Institute, String>() {
      @Override
      public String convert(Institute institute) {
        return institute.getName();
      }
    };
  }

  public void registerConverters(final FormatterRegistry registry) {
    // physical ports
    registry.addConverter(getIdToPhysicalPortConverter());
    registry.addConverter(getStringToPhysicalPortConverter());
    registry.addConverter(getPhysicalPortToStringConverter());

    // physical resource groups
    registry.addConverter(getIdToPhysicalResourceGroupConverter());
    registry.addConverter(getStringToPhysicalResourceGroupConverter());
    registry.addConverter(getPhysicalResourceGroupToStringConverter());

    // virtual resource groups
    registry.addConverter(getIdToVirtualResourceGroupConverter());
    registry.addConverter(getStringToVirtualResourceGroupConverter());
    registry.addConverter(getVirtualResourceGroupToStringConverter());

    // virtual ports
    registry.addConverter(getIdToVirtualPortConverter());
    registry.addConverter(getStringToVirtualPortConverter());
    registry.addConverter(getVirtualPortToStringConverter());

    // reservation end points
    registry.addConverter(getVirtualPortIdToReservationEndPointConverter());
    registry.addConverter(getVirtualPortIdAsStringToReservationEndPointConverter());

    // virtual port request links
    registry.addConverter(getIdToVirtualPortRequestLinkConverter());
    registry.addConverter(getStringToVirtualPortRequestLinkConverter());

    // Institute
    registry.addConverter(getInstituteToStringConverter());

    registry.addFormatterForFieldAnnotation(new NumberFormatAnnotationFormatterFactory());
    registry.addConverter(getXmlGregorianCalendarToStringConverter());

    JodaTimeFormattingPatternConfigurer jodaTimeFormattingPatternConfigurer = new JodaTimeFormattingPatternConfigurer().setDateTimePattern("yyyy-MM-dd H:mm").setTimePattern("H:mm")
        .setDatePattern("yyyy-MM-dd");

    jodaTimeFormattingPatternConfigurer.registerFormatters(registry);
  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    registerConverters(getObject());
  }
}

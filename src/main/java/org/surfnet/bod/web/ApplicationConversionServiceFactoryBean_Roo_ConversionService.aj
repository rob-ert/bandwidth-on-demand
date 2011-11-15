// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.surfnet.bod.web;

import java.lang.Long;
import java.lang.String;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.surfnet.bod.physicalresourcegroup.PhysicalResourceGroup;

privileged aspect ApplicationConversionServiceFactoryBean_Roo_ConversionService {
    
    declare @type: ApplicationConversionServiceFactoryBean: @Configurable;
    
    public Converter<PhysicalResourceGroup, String> ApplicationConversionServiceFactoryBean.getPhysicalResourceGroupToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<org.surfnet.bod.physicalresourcegroup.PhysicalResourceGroup, java.lang.String>() {
            public String convert(PhysicalResourceGroup physicalResourceGroup) {
                return new StringBuilder().append(physicalResourceGroup.getName()).append(" ").append(physicalResourceGroup.getInstitutionName()).toString();
            }
        };
    }
    
    public Converter<Long, PhysicalResourceGroup> ApplicationConversionServiceFactoryBean.getIdToPhysicalResourceGroupConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, org.surfnet.bod.physicalresourcegroup.PhysicalResourceGroup>() {
            public org.surfnet.bod.physicalresourcegroup.PhysicalResourceGroup convert(java.lang.Long id) {
                return PhysicalResourceGroup.findPhysicalResourceGroup(id);
            }
        };
    }
    
    public Converter<String, PhysicalResourceGroup> ApplicationConversionServiceFactoryBean.getStringToPhysicalResourceGroupConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, org.surfnet.bod.physicalresourcegroup.PhysicalResourceGroup>() {
            public org.surfnet.bod.physicalresourcegroup.PhysicalResourceGroup convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), PhysicalResourceGroup.class);
            }
        };
    }
    
    public void ApplicationConversionServiceFactoryBean.installLabelConverters(FormatterRegistry registry) {
        registry.addConverter(getPhysicalResourceGroupToStringConverter());
        registry.addConverter(getIdToPhysicalResourceGroupConverter());
        registry.addConverter(getStringToPhysicalResourceGroupConverter());
    }
    
    public void ApplicationConversionServiceFactoryBean.afterPropertiesSet() {
        super.afterPropertiesSet();
        installLabelConverters(getObject());
    }
    
}

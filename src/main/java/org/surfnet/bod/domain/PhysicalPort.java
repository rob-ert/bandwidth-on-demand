package org.surfnet.bod.domain;

import javax.persistence.ManyToOne;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooEntity
public class PhysicalPort {

	private String name;

	@ManyToOne
	private PhysicalResourceGroup physicalResourceGroup;
}

package org.surfnet.bod.institution;

import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooEntity
public class Institution {

	@NotNull
	private String name;

	@ManyToOne
	private org.surfnet.bod.physicalresourcegroup.PhysicalResourceGroup physicalResourceGroup;
}

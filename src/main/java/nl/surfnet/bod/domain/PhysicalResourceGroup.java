package nl.surfnet.bod.domain;

import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooEntity
public class PhysicalResourceGroup {

	@NotNull
	private String name;

	@NotNull
	private String institutionName;

}

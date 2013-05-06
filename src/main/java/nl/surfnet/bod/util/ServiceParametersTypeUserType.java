package nl.surfnet.bod.util;

import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;

public class ServiceParametersTypeUserType extends JaxbUserType<ServiceParametersType> {

    public ServiceParametersTypeUserType() {
        super(ServiceParametersType.class);
    }

}

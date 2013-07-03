package nl.surfnet.bod;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

/**
 * Causes the webservice that listens for onecontrol notifications to be initialized, when the 'onecontrol' Spring-profile
 * is active. It is ignored otherwise
 */
@Configuration
@Profile("onecontrol")
@ImportResource({ "classpath:spring/appCtx-ws-onecontrol-consumer.xml" })
public class OnecontrolConsumerConfiguration {

}

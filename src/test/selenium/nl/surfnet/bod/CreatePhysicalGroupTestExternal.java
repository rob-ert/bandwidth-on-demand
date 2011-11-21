package nl.surfnet.bod;

import nl.surfnet.bod.support.TestExternalSupport;

import org.junit.Test;

public class CreatePhysicalGroupTestExternal extends TestExternalSupport {

    @Test
    public void createPhysicalGroup() {
        getWebDriver().createNewPhysicalGroup("My first group");
        
        getWebDriver().verifyGroupWasCreated("My first group");
    }
}

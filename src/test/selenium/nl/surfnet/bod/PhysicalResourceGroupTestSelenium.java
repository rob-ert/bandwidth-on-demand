package nl.surfnet.bod;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.TestExternalSupport;

import org.junit.Test;

public class PhysicalResourceGroupTestSelenium extends TestExternalSupport {

    @Test
    public void createPhysicalGroup() throws Exception {
        getWebDriver().createNewPhysicalGroup("My first group");

        getWebDriver().verifyGroupWasCreated("My first group");
    }

    @Test
    public void deletePhysicalGroup() throws Exception {
        PhysicalResourceGroup group = givenAPhysicalResourceGroup("Delete this group");

        getWebDriver().deletePhysicalGroup(group);

        getWebDriver().verifyGroupWasDeleted(group);
    }

    private PhysicalResourceGroup givenAPhysicalResourceGroup(String groupName) throws Exception {
        PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setName(groupName).create();

        getWebDriver().createNewPhysicalGroup(group.getName());

        return group;
    }
}

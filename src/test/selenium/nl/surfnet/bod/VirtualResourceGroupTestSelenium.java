package nl.surfnet.bod;

import static org.junit.Assert.assertTrue;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.pages.virtualresourcegroup.NewVirtualResourceGroupPage;
import nl.surfnet.bod.support.TestExternalSupport;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Before;
import org.junit.Test;

public class VirtualResourceGroupTestSelenium extends TestExternalSupport {

  @Before
  public void setUp() {
    getWebDriver().performLogin("TestUser");
  }

  @Test
  public void createVirtualResourceGroup() throws Exception {
    givenAVirtualResourceGroup("vrg1");

    getWebDriver().verifyVirtualResourceGroupWasCreated("vrg1");
  }

  @Test
  public void createExistingVirtualResourceGroup() throws Exception {
    givenAVirtualResourceGroup("vrg2");
    getWebDriver().verifyVirtualResourceGroupWasCreated("vrg2");

    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setSurfConnextGroupName("vrg2").create();
    NewVirtualResourceGroupPage virtualResourceGroupPage = getWebDriver().createNewVirtualResourceGroup(
        vrg.getSurfConnextGroupName());

    assertTrue(virtualResourceGroupPage.hasErrorSurfConnextGroupName());    
  }

  @Test
  public void deleteVirtualResourceGroup() throws Exception {
    VirtualResourceGroup vrg = givenAVirtualResourceGroup("vrgToDelete");

    getWebDriver().deleteVirtualResourceGroup(vrg);

    getWebDriver().verifyVirtualResourceGroupWasDeleted(vrg);
  }

  private VirtualResourceGroup givenAVirtualResourceGroup(String groupName) throws Exception {
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setSurfConnextGroupName(groupName).create();

    getWebDriver().createNewVirtualResourceGroup(vrg.getSurfConnextGroupName());

    return vrg;
  }
}

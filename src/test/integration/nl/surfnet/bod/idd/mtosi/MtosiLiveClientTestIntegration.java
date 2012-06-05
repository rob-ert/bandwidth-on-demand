package nl.surfnet.bod.idd.mtosi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import nl.surfnet.bod.mtosi.MtosiLiveClient;

import org.junit.Before;
import org.junit.Test;
import org.tmforum.mtop.nrf.xsd.invdata.v1.InventoryDataType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagedElementInventoryType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagementDomainInventoryType;

import com.google.common.collect.Iterables;

public class MtosiLiveClientTestIntegration {

  private MtosiLiveClient subject;

  @Before
  public void init() {
    subject = new MtosiLiveClient();
    subject.init();
  }

  @Test
  public void retreiveInventory() {
    InventoryDataType inventory = subject.getInventory();

    assertThat(inventory, notNullValue());

    List<ManagementDomainInventoryType> mdits = inventory.getMdList().getMd();

    assertThat(mdits, hasSize(1));

    ManagementDomainInventoryType mdit = Iterables.getOnlyElement(mdits);
    List<ManagedElementInventoryType> meits = mdit.getMeList().getMeInv();

    assertThat(meits, hasSize(greaterThan(0)));
  }
}

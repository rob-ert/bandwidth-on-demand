package nl.surfnet.bod.mtosi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.tmforum.mtop.nrf.xsd.invdata.v1.InventoryDataType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagedElementInventoryType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagementDomainInventoryType;

import com.google.common.collect.Iterables;

public class MtosiLiveClientTestIntegration {

  private MtosiLiveClient subject;

  @Before
  public void init() throws IOException {
    Properties props = new Properties();
    props.load(new ClassPathResource("bod-default.properties").getInputStream());

    subject = new MtosiLiveClient(props.getProperty("mtosi.inventory.retrieval.endpoint"), "http://atlas.dlp.surfnet.nl");
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

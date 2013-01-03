package nl.surfnet.bod.web.noc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import nl.surfnet.bod.service.ReportingService;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.YearMonth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(MockitoJUnitRunner.class)
public class ReportControllerTest {

  @InjectMocks
  private ReportController subject;

  @Mock
  private ReportingService reportServiceMock;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(subject).build();
  }

  @Test
  public void graphDataShouldReturnCsvFile() throws Exception {
    ReservationReportView dummyReport = new ReservationReportView(DateTime.now().minusMonths(1), DateTime.now());

    when(reportServiceMock.determineReportForNoc(any(Interval.class))).thenReturn(dummyReport);

    mockMvc.perform(get("/noc/report/data.csv"))
      .andExpect(status().isOk())
      .andExpect(content().contentType("text/csv"))
      .andExpect(content().string(containsString("Month,Create,Create_f,Cancel,Cancel_f,NSI,NSI_f")))
      .andExpect(content().string(containsString(YearMonth.now().toString("MMM"))));
  }
}

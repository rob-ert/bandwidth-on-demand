package nl.surfnet.bod.event;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import nl.surfnet.bod.domain.Loggable;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEventType;

import org.joda.time.DateTime;

import com.google.common.base.Charsets;

public class EntityStatistics<T extends Loggable> {

  private final String domainObjectClass;
  private final DateTime periodStart;
  private final DateTime periodEnd;
  private final long amountCreated;
  private final long amountUpdated;
  private final long amountDeleted;

  public EntityStatistics(Class<T> entity, final DateTime periodStart, final long amountCreated,
      final long amountUpdated, final long amountDeleted, final DateTime periodEnd) {
    super();

    this.domainObjectClass = LogEvent.getDomainObjectName(entity);

    this.periodStart = periodStart;
    this.periodEnd = periodEnd;

    this.amountCreated = amountCreated;
    this.amountUpdated = amountUpdated;
    this.amountDeleted = amountDeleted;
  }

  public String getDomainObjectClass() {
    return domainObjectClass;
  }

  public String getDomainObjectKey() {
    return ("label_" + domainObjectClass).toLowerCase();
  }

  public DateTime getPeriodStart() {
    return periodStart;
  }

  public DateTime getPeriodEnd() {
    return periodEnd;
  }

  public long getAmountCreated() {
    return amountCreated;
  }

  public long getAmountUpdated() {
    return amountUpdated;
  }

  public long getAmountDeleted() {
    return amountDeleted;
  }

  private StringBuffer getBasicSearchCriteria() {
    StringBuffer searchCrit = new StringBuffer("domainObjectClass:").append("\"").append(getDomainObjectClass())
        .append("\"").append(" AND ");

    // TODO Frany fix dateRange
    // searchCrit.append("created:").append("[").append(TimeStampBridge.convert(periodStart));
    // searchCrit.append(" TO ").append(TimeStampBridge.convert(periodEnd)).append("]").append(" AND ");

    return searchCrit;
  }

  public String getSearchCriteriaForCreate() throws UnsupportedEncodingException {
    return URLEncoder.encode(getBasicSearchCriteria().append("eventType:").append("\"").append(
        LogEventType.CREATE.name().toLowerCase()).append("\"").toString(), Charsets.UTF_8.name());
  }

  public String getSearchCriteriaForUpdate() {
    return getBasicSearchCriteria().append("eventType:").append("\"").append(LogEventType.UPDATE.name().toLowerCase())
        .append("\"").toString();
  }

  public String getSearchCriteriaForDelete() throws UnsupportedEncodingException {
    return URLEncoder.encode(getBasicSearchCriteria().append("eventType:").append("\"").append(
        LogEventType.DELETE.name().toLowerCase()).append("\"").toString(), Charsets.UTF_8.name());
  }
}

package nl.surfnet.bod.web.view;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class JsonLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

  private static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd H:mm");

  @Override
  public void serialize(LocalDateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
      JsonProcessingException {
    jgen.writeString(formatter.print(value));
  }

}

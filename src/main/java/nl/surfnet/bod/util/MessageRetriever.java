package nl.surfnet.bod.util;

import java.util.Arrays;
import java.util.Locale;

import javax.annotation.Resource;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

@Component
public class MessageRetriever {

  public static final String INFO_MESSAGES_KEY = "infoMessages";
  public static final String ERROR_MESSAGES_KEY = "errorMessages";
  public static final String WARN_MESSAGES_KEY = "warnMessages";

  @Resource
  private MessageSource messageSource;

  public String getMessageWithBoldArguments(String key, String... args) {
    return getMessage(key, makeArgsDisplayBold(args));
  }

  public String getMessage(String key, String... args) {
    return messageSource.getMessage(key, args, getDefaultLocale());
  }

  private String[] makeArgsDisplayBold(String[] objects) {
    return FluentIterable.from(Arrays.asList(objects)).transform(new Function<String, String>() {
      @Override
      public String apply(String input) {
        return String.format("<b>%s</b>", input);
      }
    }).toArray(String.class);
  }

  private Locale getDefaultLocale() {
    return LocaleContextHolder.getLocale();
  }

}

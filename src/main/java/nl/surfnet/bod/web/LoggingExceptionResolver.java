package nl.surfnet.bod.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

public class LoggingExceptionResolver extends SimpleMappingExceptionResolver {

  private final Logger logger = LoggerFactory.getLogger(LoggingExceptionResolver.class);

  @Override
  protected ModelAndView doResolveException(HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      Exception ex) {
    logger.error("An exception occured during user request", ex);

    return super.doResolveException(request, response, handler, ex);
  }
}

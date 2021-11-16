package com.dtcc.ion.tracing.filters;

import com.dtcc.ion.tracing.IonTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
public class ControllerRequestFilter implements Filter {

  @Autowired
  private IonTracer ionTracer;

  @Override
  public void init(final FilterConfig filterConfig) {
    log.info("Initializing request filter: {}", this);
  }

  @Override
  public void doFilter(
      ServletRequest request,
      ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    Map<String, Object> headersMap = Collections.list(req.getHeaderNames())
        .stream()
        .collect(Collectors.toMap(
            Function.identity(),
            req::getHeader
        ));

    SpanContext context = ionTracer.extractHTTP(headersMap);

    String opName = req.getRequestURI();
    Span span = ionTracer.buildSpan(opName, context);
    try (Scope scope = ionTracer.activate(span)) {
      chain.doFilter(request, response);
    } catch (Exception exception) {
      Tags.ERROR.set(span, true);
      span.log(Map.of(Fields.EVENT, "error", Fields.ERROR_OBJECT, exception, Fields.MESSAGE, exception.getMessage()));
      throw exception;
    } finally {
      span.finish();
    }
  }

}
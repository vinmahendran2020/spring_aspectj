package com.dtcc.ion.tracing;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.propagation.TextMapAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IonTracer {

  @Autowired
  private Tracer tracer;

  public SpanContext extractJMS(Map<String, Object> carrier) {
    HashMap<String, String> carrierCopy = new HashMap<>();

    for (Entry<String, Object> entry : carrier.entrySet()) {
      if (entry.getValue() instanceof String) {
        String key = entry.getKey();
        carrierCopy.put(key.replace("_", "-"), (String) entry.getValue());
      }
    }
    return tracer.extract(Builtin.TEXT_MAP, new TextMapAdapter(carrierCopy));
  }

  public SpanContext extractHTTP(Map<String, Object> carrier) {
    Map<String, String> carrierCopy = carrier.entrySet()
        .stream()
        .filter(h -> h.getValue() instanceof String)
        .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));

    return tracer.extract(Builtin.HTTP_HEADERS, new TextMapAdapter(carrierCopy));
  }

  public HashMap<String, String> injectJMS() {
    HashMap<String, String> carrier = new HashMap<>();
    tracer.inject(tracer.activeSpan().context(), Builtin.TEXT_MAP, new TextMapAdapter(carrier));

    return (HashMap<String, String>) carrier.entrySet()
        .stream()
        .collect(Collectors.toMap(e -> (e.getKey()).replace("-", "_"), e -> e.getValue()));
  }

  public HashMap<String, String> injectHTTP() {
    HashMap<String, String> carrier = new HashMap<>();
    tracer.inject(tracer.activeSpan().context(), Builtin.HTTP_HEADERS, new TextMapAdapter(carrier));

    return carrier;
  }

  public Span buildSpan(String opName) {
    return buildSpan(opName, null);
  }

  public Span buildSpan(String opName, SpanContext context) {
    Span span;
    if (context == null) {
      span = tracer.buildSpan(opName).start();
    } else {
      span = tracer.buildSpan(opName).asChildOf(context).start();
    }
    return span;
  }

  public Scope activate(Span span) {
    return tracer.scopeManager().activate(span);
  }

}

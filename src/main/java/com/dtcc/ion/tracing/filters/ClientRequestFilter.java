package com.dtcc.ion.tracing.filters;

import com.dtcc.ion.tracing.IonTracer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;


public class ClientRequestFilter {

  public static ExchangeFilterFunction filter(IonTracer ionTracer) {
    ExchangeFilterFunction fun = (clientRequest, nextFilter) -> {

      Map<String, String> context = ionTracer.injectHTTP();
      MultiValueMap<String, String> mvMap = CollectionUtils.toMultiValueMap(context.entrySet()
          .stream()
          .collect(Collectors.toMap(e -> (String) e.getKey(), e -> List.of(e.getValue()))));

      ClientRequest filtered = ClientRequest.from(clientRequest)
          .headers(headers -> headers.addAll(mvMap))
          .build();

      return nextFilter.exchange(filtered);
    };
    return fun;
  }

}
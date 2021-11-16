package com.dtcc.ion.tracing;

import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class IonTracerConfiguration {

  @Value("${jaeger.service.name}")
  private String serviceName;

  @Bean
  public Tracer getTracer() {
    return io.jaegertracing.Configuration.fromEnv(serviceName).getTracer();
  }

}

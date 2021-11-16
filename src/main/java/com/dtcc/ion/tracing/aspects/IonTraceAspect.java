package com.dtcc.ion.tracing.aspects;

import com.dtcc.ion.tracing.IonTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class IonTraceAspect {

  @Autowired
  private IonTracer ionTracer;

  @Pointcut("@annotation(com.dtcc.ion.tracing.IonTrace)")
  private void inIon() {}

  @Around("inIon()")
  public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
    Object proceed;
    Signature sig = joinPoint.getSignature();
    String opName = sig.getName();

    Span span = ionTracer.buildSpan(opName);
    try (Scope scope = ionTracer.activate(span)) {
      proceed = joinPoint.proceed();
    } catch (Exception exception) {
      Tags.ERROR.set(span, true);
      span.log(Map.of(Fields.EVENT, "error", Fields.ERROR_OBJECT, exception, Fields.MESSAGE, exception.getMessage()));
      throw exception;
    } finally {
      span.finish();
    }
    return proceed;
  }

}

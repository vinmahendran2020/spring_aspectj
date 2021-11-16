package com.dtcc.ion.tracing.aspects;

import com.dtcc.ion.tracing.IonTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
public class JMSAspect {

  @Autowired
  private IonTracer ionTracer;

  @Pointcut("execution(* com.dtcc.ion.ionmqstarter.sender.MessageSender.send(..))")
  private void messageSenderSend() {}

  @Pointcut("execution(* com.dtcc.ion.ionmqstarter.sender.recoverable.RecoverableMessageSender.send(..))")
  private void recoverableMessageSenderSend() {}

  @Pointcut("@annotation(org.springframework.jms.annotation.JmsListener)")
  private void jmsListener() {}

  @Around("messageSenderSend() || recoverableMessageSenderSend()")
  public Object injectJMSContext(ProceedingJoinPoint joinPoint) throws Throwable {
    Object[] args = joinPoint.getArgs();

    if (args.length > 2 && args[2] instanceof HashMap) {
      HashMap<String, String> headers = (HashMap<String, String>) args[2];
      headers.putAll(ionTracer.injectJMS());
      log.info(headers.toString());
    }

    return joinPoint.proceed(args);
  }

  @Around("jmsListener()")
  public Object extractJMSContext(ProceedingJoinPoint joinPoint) throws Throwable {
    Object proceed;
    Signature sig = joinPoint.getSignature();
    String opName = sig.getName();

    HashMap<String, Object> headers = new HashMap<>();
    Object[] args = joinPoint.getArgs();

    if (args.length > 1 && args[1] instanceof Map) {
      headers.putAll((Map<String, Object>) args[1]);
    }

    SpanContext context = ionTracer.extractJMS(headers);
    Span span = ionTracer.buildSpan(opName, context);

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

package hugo.weaving.internal;

import android.os.Build;
import android.os.Looper;
import android.os.Trace;
import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.concurrent.TimeUnit;

@Aspect
public class Hugo {
  private static volatile boolean enabled = true;

  @Pointcut("within(@hugo.weaving.DebugLog *)")
  public void withinAnnotatedClass() {}

  @Pointcut("execution(!synthetic * *(..)) && withinAnnotatedClass()")
  public void methodInsideAnnotatedType() {}

  @Pointcut("execution(!synthetic *.new(..)) && withinAnnotatedClass()")
  public void constructorInsideAnnotatedType() {}

  @Pointcut("execution(@hugo.weaving.DebugLog * *(..)) || methodInsideAnnotatedType()")
  public void method() {}

  @Pointcut("execution(@hugo.weaving.DebugLog *.new(..)) || constructorInsideAnnotatedType()")
  public void constructor() {}


  @Pointcut("execution(@hugo.weaving.ErrorLog * *(..))")
  public void errorMethod() {}

  @Pointcut("execution(@hugo.weaving.ErrorLog *.new(..))")
  public void errorConstructor() {}


  @Pointcut("execution(@hugo.weaving.InfoLog * *(..))")
  public void infoMethod() {}

  @Pointcut("execution(@hugo.weaving.InfoLog *.new(..))")
  public void infoConstructor() {}


  @Pointcut("execution(@hugo.weaving.VerboseLog * *(..))")
  public void verbMethod() {}

  @Pointcut("execution(@hugo.weaving.VerboseLog *.new(..))")
  public void verbConstructor() {}


  public static void setEnabled(boolean enabled) {
    Hugo.enabled = enabled;
  }

  enum LOG_TYPE {
    ERROR, INFO, VERBOSE, DEBUG
  }

  @Around("method() || constructor()")
  public Object logDebugAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {
    enterMethod(joinPoint, LOG_TYPE.DEBUG);

    Object result = joinPoint.proceed();
    exitMethod(joinPoint, result, lengthMillies(), LOG_TYPE.DEBUG);

    return result;
  }

  @Around("errorMethod() || errorConstructor()")
  public Object logErrorAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {
    enterMethod(joinPoint, LOG_TYPE.ERROR);

    Object result = joinPoint.proceed();
    exitMethod(joinPoint, result, lengthMillies(), LOG_TYPE.ERROR);

    return result;
  }

  @Around("infoMethod() || infoConstructor()")
  public Object logInfoAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {
    enterMethod(joinPoint, LOG_TYPE.INFO);

    Object result = joinPoint.proceed();
    exitMethod(joinPoint, result, lengthMillies(), LOG_TYPE.INFO);

    return result;
  }

  @Around("verbMethod() || verbConstructor()")
  public Object logVerbAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {
    enterMethod(joinPoint, LOG_TYPE.VERBOSE);

    Object result = joinPoint.proceed();
    exitMethod(joinPoint, result, lengthMillies(), LOG_TYPE.VERBOSE);

    return result;
  }

  private static long lengthMillies () {
    long startNanos = System.nanoTime();
    long stopNanos = System.nanoTime();
    return TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);
  }

  private static void enterMethod(JoinPoint joinPoint, LOG_TYPE type) {
    if (!enabled) return;

    CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

    Class<?> cls = codeSignature.getDeclaringType();
    String methodName = codeSignature.getName();
    String[] parameterNames = codeSignature.getParameterNames();
    Object[] parameterValues = joinPoint.getArgs();

    StringBuilder builder = new StringBuilder("\u21E2 ");
    builder.append(methodName).append('(');
    for (int i = 0; i < parameterValues.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(parameterNames[i]).append('=');
      builder.append(Strings.toString(parameterValues[i]));
    }
    builder.append(')');

    if (Looper.myLooper() != Looper.getMainLooper()) {
      builder.append(" [Thread:\"").append(Thread.currentThread().getName()).append("\"]");
    }

    logString(asTag(cls), builder.toString(), type);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      final String section = builder.toString().substring(2);
      Trace.beginSection(section);
    }
  }

  private static void logString(String tag, String data, LOG_TYPE type) {
    switch (type) {
      case VERBOSE:
        Log.v(tag, data);
        break;

      case DEBUG:
        Log.d(tag, data);
        break;

      case INFO:
        Log.i(tag, data);
        break;

      case ERROR:
        Log.e(tag, data);
        break;
    }
  }

  private static void exitMethod(JoinPoint joinPoint, Object result, long lengthMillis, LOG_TYPE type) {
    if (!enabled) return;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      Trace.endSection();
    }

    Signature signature = joinPoint.getSignature();

    Class<?> cls = signature.getDeclaringType();
    String methodName = signature.getName();
    boolean hasReturnType = signature instanceof MethodSignature
        && ((MethodSignature) signature).getReturnType() != void.class;

    StringBuilder builder = new StringBuilder("\u21E0 ")
        .append(methodName)
        .append(" [")
        .append(lengthMillis)
        .append("ms]");

    if (hasReturnType) {
      builder.append(" = ");
      builder.append(Strings.toString(result));
    }

    logString(asTag(cls), builder.toString(), type);

  }

  private static String asTag(Class<?> cls) {
    if (cls.isAnonymousClass()) {
      return asTag(cls.getEnclosingClass());
    }
    return cls.getSimpleName();
  }
}

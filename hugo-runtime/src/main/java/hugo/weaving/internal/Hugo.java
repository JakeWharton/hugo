package hugo.weaving.internal;

import android.util.Log;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class Hugo {
  private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$\\d+$");

  @Pointcut("execution(@hugo.weaving.DebugLog * *(..))")
  public void debugLog() {}

  @Around("debugLog()")
  public Object debugLogMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    pushMethod(joinPoint);

    long startNanos = System.nanoTime();
    Object result = joinPoint.proceed();
    long stopNanos = System.nanoTime();
    long lengthMillis = TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);

    popMethod(joinPoint, result, lengthMillis);

    return result;
  }

  private static void pushMethod(JoinPoint joinPoint) {
    MethodSignature codeSignature = (MethodSignature) joinPoint.getSignature();

    String className = codeSignature.getDeclaringTypeName();
    String methodName = codeSignature.getName();
    String[] parameterNames = codeSignature.getParameterNames();
    Object[] parameterValues = joinPoint.getArgs();

    StringBuilder builder = new StringBuilder("⇢ ");
    builder.append(methodName).append('(');
    for (int i = 0; i < parameterValues.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(parameterNames[i]).append('=');
      appendObject(builder, parameterValues[i]);
    }
    builder.append(')');

    Log.d(asTag(className), builder.toString());
  }

  private static void popMethod(JoinPoint joinPoint, Object result, long lengthMillis) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();

    String className = signature.getDeclaringTypeName();
    String methodName = signature.getName();
    boolean hasReturnType = signature.getReturnType() != void.class;

    StringBuilder builder = new StringBuilder("⇠ ")
        .append(methodName)
        .append(" [")
        .append(lengthMillis)
        .append("ms]");

    if (hasReturnType) {
      builder.append(" = ");
      appendObject(builder, result);
    }

    Log.d(asTag(className), builder.toString());
  }

  private static void appendObject(StringBuilder builder, Object value) {
    if (value == null) {
      builder.append("null");
    } else if (value instanceof String) {
      builder.append('"').append(value).append('"');
    } else if (value.getClass().isArray()) {
      builder.append(arrayToString(value));
    } else {
      builder.append(value.toString());
    }
  }

  private static String asTag(String className) {
    Matcher m = ANONYMOUS_CLASS.matcher(className);
    if (m.find()) {
      className = m.replaceAll("");
    }
    return className.substring(className.lastIndexOf('.') + 1);
  }

  private static String arrayToString(final Object o) {
    if (o instanceof Object[]) {
      return Arrays.toString((Object[]) o);
    }

    // Must be primitive array.
    Class<?> clazz = o.getClass();

    if (byte[].class.equals(clazz)) {
      return byteArrayToString((byte[]) o);
    }
    if (short[].class.equals(clazz)) {
      return Arrays.toString((short[]) o);
    }
    if (char[].class.equals(clazz)) {
      return Arrays.toString((char[]) o);
    }
    if (int[].class.equals(clazz)) {
      return Arrays.toString((int[]) o);
    }
    if (long[].class.equals(clazz)) {
      return Arrays.toString((long[]) o);
    }
    if (float[].class.equals(clazz)) {
      return Arrays.toString((float[]) o);
    }
    if (double[].class.equals(clazz)) {
      return Arrays.toString((double[]) o);
    }
    if (boolean[].class.equals(clazz)) {
      return Arrays.toString((boolean[]) o);
    }
    throw new IllegalArgumentException("Unknown array type: " + clazz);
  }

  /** A more human-friendly version of Arrays.toString(byte[]) that uses hex representation. */
  private static String byteArrayToString(byte[] bytes) {
    StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < bytes.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(String.format("%02x", bytes[i]));
    }
    return builder.append(']').toString();
  }
}

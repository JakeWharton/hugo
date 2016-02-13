package hugo.weaving.internal;

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

/**
 * Created by williamwebb on 2/13/16.
 */
@Aspect @SuppressWarnings("unused")
public final class ProfileAspect {

  private static final int CALL_STACK_INDEX = 4;

  private static final String FIELD_GET = "field-get";
  private static final String FIELD_SET = "field-set";
  private static final String METHOD_CALL = "method-call";
  private static final String CONSTRUCTOR_CALL = "constructor-call";

  // Don't interfere with @DebugLog
  @Pointcut("call(@hugo.weaving.DebugLog * *(..))")
  void hasDebugLog() {}

  @Pointcut("withincode(@hugo.weaving.Profile * *(..)) && !hasDebugLog()")
  void inMethod() {}

  @Pointcut("withincode(@hugo.weaving.Profile *.new(..)) && !hasDebugLog()")
  void inConstructor() {}

  @Around("inMethod() || inConstructor()")
  public Object profileAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {
    long startNanos = System.nanoTime();
    Object result = joinPoint.proceed();
    long stopNanos = System.nanoTime();
    long lengthMillis = TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);

    switch (joinPoint.getKind()) {
      case METHOD_CALL: printMethod(joinPoint, result, lengthMillis); break;
      case CONSTRUCTOR_CALL: printConstructor(joinPoint, result, lengthMillis); break;
      default: return joinPoint.proceed();
    }

    return result;
  }

  private static void printMethod(JoinPoint joinPoint, Object result, long lengthMillis) {
    if (!Hugo.isEnabled()) return;

    Signature signature = joinPoint.getSignature();

    StringBuilder builder = new StringBuilder("- ");
    builder
      .append(getParentMethodName())
      .append(" \u21E2 ")
      .append(getExecutingMethodName(joinPoint));

    CodeSignature codeSignature = (CodeSignature) signature;
    String[] parameterNames = codeSignature.getParameterNames();
    Object[] parameterValues = joinPoint.getArgs();

    builder.append('(');
    for (int i = 0; i < parameterValues.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(parameterNames[i]).append('=');
      builder.append(Strings.toString(parameterValues[i]));
    }
    builder.append(')');

    builder.append(" [").append(lengthMillis).append("ms]");

    boolean hasReturnType =
        signature instanceof MethodSignature &&
            ((MethodSignature) signature).getReturnType() != void.class;

    if (hasReturnType) {
      builder.append(" = ");
      builder.append(Strings.toString(result));
    }

    Log.v(asTag(joinPoint), builder.toString());
  }

  private static void printConstructor(JoinPoint joinPoint, Object result, long lengthMillis) {
    if (!Hugo.isEnabled()) return;

    Signature signature = joinPoint.getSignature();

    String parentMethodName = getParentMethodName();
    String methodClassName = signature.getDeclaringType().getSimpleName();
    String methodName = signature.getName();

    StringBuilder builder = new StringBuilder("- ");
    builder
      .append(parentMethodName)
      .append(" \u21E2 ")
      .append(methodClassName)
      .append("::")
      .append(methodName);

    CodeSignature codeSignature = (CodeSignature) signature;
    String[] parameterNames = codeSignature.getParameterNames();
    Object[] parameterValues = joinPoint.getArgs();

    builder.append('(');
    for (int i = 0; i < parameterValues.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(parameterNames[i]).append('=');
      builder.append(Strings.toString(parameterValues[i]));
    }
    builder.append(')');

    builder.append(" [").append(lengthMillis).append("ms]");

    Log.v(asTag(joinPoint), builder.toString());
  }

  /** don't switch to {@code Thread.currentThread }, no performance gains */
  private static String getParentMethodName() {
    StackTraceElement[] stackTrace = new Throwable().getStackTrace();
    if (stackTrace.length <= CALL_STACK_INDEX) {
      throw new IllegalStateException(
          "Synthetic stacktrace didn't have enough elements: are you using proguard?");
    }
    String methodName = stackTrace[CALL_STACK_INDEX].getMethodName();

    int index = methodName.indexOf('_');

    return (index == -1 ? methodName : methodName.substring(0,methodName.indexOf('_')));
  }

  private static String getExecutingMethodName(JoinPoint joinPoint) {
    Class withinType = joinPoint.getStaticPart().getSourceLocation().getWithinType();
    Class declaredType = joinPoint.getStaticPart().getSignature().getDeclaringType();

    // inner-classes
    if (declaredType.isLocalClass() || declaredType.isMemberClass()) {
      declaredType = declaredType.getEnclosingClass();
    }

    String methodName = joinPoint.getSignature().getName();
    String classname = ""; // if call trace is different then parent class, add to method signature
    if(withinType != declaredType) {
      classname = joinPoint.getSignature().getDeclaringType().getSimpleName();
    }

    return classname + "." + methodName;
  }

  private static String asTag(JoinPoint joinPoint) {
    return joinPoint.getStaticPart().getSourceLocation().getWithinType().getSimpleName();
  }

}
package hugo.weaving.internal;


import hugo.weaving.DebugLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
public class Hugo {

    private static Logger logger = new Logger() {
        @Override
        public void log(String clazz, String message) {
            System.out.println(clazz + ": " + message);
        }
    };

    public static interface Logger {
        public void log(String clazz, String message);
    }

    public static void setLogger(Logger logger) {
        Hugo.logger = logger;
    }

  @Pointcut("execution(@hugo.weaving.DebugLog * *(..))")
  public void method() {}

  @Pointcut("execution(@hugo.weaving.DebugLog *.new(..))")
  public void constructor() {}

  @Around("method() || constructor()")
  public Object logAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {
    CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
    boolean showInputs, showOutput;
    try {
     String methodName = codeSignature.getName();
     Class<?> clazz = codeSignature.getDeclaringType();
     Class<?>[] methodParams = codeSignature.getParameterTypes();
     Method m = clazz.getDeclaredMethod(methodName, methodParams);

     DebugLog debugLog = m.getAnnotation(DebugLog.class);
     showInputs = debugLog.input();
     showOutput = debugLog.output();
    } catch (java.lang.NoSuchMethodException e){
        showInputs = true;
        showOutput = true;
    }

    pushMethod(joinPoint,showInputs);

    long startNanos = System.nanoTime();
    Object result = joinPoint.proceed();
    long stopNanos = System.nanoTime();
    long lengthMillis = TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);

    popMethod(joinPoint, showOutput, result, lengthMillis);

    return result;
  }

  private static void pushMethod(JoinPoint joinPoint, boolean showInputs) {
    CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

    Class<?> clazz = codeSignature.getDeclaringType();
    String methodName = codeSignature.getName();
    String[] parameterNames = codeSignature.getParameterNames();
    Object[] parameterValues = joinPoint.getArgs();

    StringBuilder builder = new StringBuilder("\u21E2 ");
    if(showInputs) {
     builder.append(methodName).append('(');
     for (int i = 0; i < parameterValues.length; i++) {
        if (i > 0) {
            builder.append(", ");
        }
        builder.append(parameterNames[i]).append('=');
        appendObject(builder, parameterValues[i]);
     }
     builder.append(')');
    }

//    if (!isMainThread()) {
      builder.append(" @Thread:").append(Thread.currentThread().getName());
//    }

    logger.log(asTag(clazz), builder.toString());
  }

//  private static boolean isMainThread() {
//    return Looper.myLooper() == Looper.getMainLooper();
//  }

  private static void popMethod(JoinPoint joinPoint, boolean showOutput, Object result, long lengthMillis) {
    Signature signature = joinPoint.getSignature();

    Class<?> clazz = signature.getDeclaringType();
    String methodName = signature.getName();
    boolean hasReturnType = signature instanceof MethodSignature
        && ((MethodSignature) signature).getReturnType() != void.class;

    StringBuilder builder = new StringBuilder("\u21E0 ")
        .append(methodName)
        .append(" [")
        .append(lengthMillis)
        .append("ms]");

    if (hasReturnType && showOutput) {
      builder.append(" = ");
      appendObject(builder, result);
    }

    logger.log(asTag(clazz), builder.toString());
  }

  private static void appendObject(StringBuilder builder, Object value) {
    builder.append(Strings.toString(value));
  }

  private static String asTag(final Class<?> clazz) {
    if (clazz.isAnonymousClass()) {
      return asTag(clazz.getEnclosingClass());
    }
    return clazz.getSimpleName();
  }
}

package hugo.weaving;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, CONSTRUCTOR}) @Retention(RUNTIME)
public @interface DebugLog {
    public boolean input() default true;
    public boolean output() default true;
}

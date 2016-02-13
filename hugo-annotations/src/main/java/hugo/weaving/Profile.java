package hugo.weaving;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by williamwebb on 2/10/16.
 */


@Target({TYPE, METHOD, CONSTRUCTOR}) @Retention(CLASS)
public @interface Profile {
}

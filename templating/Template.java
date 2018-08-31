package de.danielhons.lib.templating;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.FIELD
})
public @interface Template {
    public enum Source {VALUE,FILE}
    String value();

    Source source() default Source.VALUE;
}

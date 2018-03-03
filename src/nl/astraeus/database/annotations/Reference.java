package nl.astraeus.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: rnentjes
 * Date: 11/13/13
 * Time: 4:13 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD } )
public @interface Reference {

    int length() default 12;

}

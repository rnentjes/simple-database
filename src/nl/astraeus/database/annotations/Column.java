package nl.astraeus.database.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * User: rnentjes
 * Date: 11/13/13
 * Time: 4:13 PM
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    String name() default "";

}

package nl.astraeus.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Date: 11/13/13
 * Time: 9:57 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD } )
public @interface Index {

    boolean unique() default false;

}

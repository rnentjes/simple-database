package nl.astraeus.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Date: 11/13/13
 * Time: 9:57 PM
 */
@Target( { ElementType.FIELD } )
public @interface Length {

    int value();

}

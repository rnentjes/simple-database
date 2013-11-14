package nl.astraeus.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Date: 11/14/13
 * Time: 9:30 PM
 */
public class Util {

    // assumes UTF-8 compatable stream content
    public static String readAsString(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte [] buffer = new byte[8196];
        int nr = 0;

        while((nr = in.read(buffer)) > 0) {
            out.write(buffer, 0, nr);
        }

        in.close();

        return new String(out.toByteArray(), Charset.forName("UTF-8"));
    }
}

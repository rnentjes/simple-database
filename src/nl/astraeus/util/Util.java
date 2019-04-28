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

    // assumes UTF-8 compatible stream content
    public static String readAsString(InputStream in) throws IOException {
        return new String(readInputStream(in), Charset.forName("UTF-8"));
    }

    public static byte [] readInputStream(InputStream in) {
        byte [] buffer = new byte[8196];
        int nr = 0;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            while((nr = in.read(buffer)) > 0) {
                out.write(buffer, 0, nr);
            }

            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

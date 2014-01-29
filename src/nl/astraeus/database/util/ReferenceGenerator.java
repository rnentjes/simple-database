package nl.astraeus.database.util;

import java.util.Random;

/**
 * Date: 1/29/14
 * Time: 9:03 PM
 */
public class ReferenceGenerator {

    private static Random random = new Random(System.nanoTime() | System.currentTimeMillis());
    private static String alphabet = "abcdefghijkmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ023456789";
    private static String base64alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-";

    public static String generateRandomReference(int length) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            result.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }

        return result.toString();
    }

    public static String generateReferenceFromId(long id) {
        StringBuilder result = new StringBuilder();

        long hash = 0;
        for (int i=0; i < 11; i++) {
            int val = (int) (id % 64);

            hash = hash * 31 + val;
        }

        hash = Math.abs(hash);

        while(hash > 0) {
            int val = (int) (hash % 64);

            result.append(base64alphabet.charAt(val));

            hash = hash >> 6;
        }

        while(result.length() < 9) {
            result.append(base64alphabet.charAt(random.nextInt(base64alphabet.length())));
        }

        return result.toString();
    }

}

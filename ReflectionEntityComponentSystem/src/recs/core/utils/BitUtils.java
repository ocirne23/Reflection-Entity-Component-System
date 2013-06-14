package recs.core.utils;

/**
 *
 * @author Enrico van Oosten
 */
public class BitUtils {
    public static byte[] createByteArr(boolean[] booleans) {
        int length = booleans.length;
        byte[] bytes = new byte[length / 8 + 1];

        for (int i = 0; i < length; i++) {
            int word = i >>> 3;
            bytes[word] |= 1 << (i & 7);
        }
        return bytes;
    }

    public static byte[] createByteArr(Boolean[] booleans) {
        int length = booleans.length;
        byte[] bytes = new byte[length / 8 + 1];

        for (int i = 0; i < length; i++) {
            int word = i >>> 3;
            bytes[word] |= 1 << (i & 7);
        }
        return bytes;
    }

    public static boolean[] getBooleans(byte[] bytes, int length) {
        boolean[] booleans = new boolean[length];

        for (int i = 0; i < length; i++) {
            final int word = i >>> 3;
            booleans[i] = (bytes[word] & (1 << (i & 7))) != 0;
        }
        return booleans;
    }
}

package util;

import java.net.URLDecoder;
import java.util.Arrays;

public class ModifiedBase64 {
    private static final char[] MODIFIED_BASE64_TABLE = "ZYXWVUTSRQPONMLKJIHGFEDCBAabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final byte[] BASE64_TABLE_REVERSE = new byte[256];

    static {
        Arrays.fill(BASE64_TABLE_REVERSE, (byte) 0xFF);
        for (int i = 0; i < MODIFIED_BASE64_TABLE.length; i++) {
            BASE64_TABLE_REVERSE[MODIFIED_BASE64_TABLE[i]] = (byte) i;
        }
    }

    public static String modifiedBase64Encode(byte[] data) {
        StringBuilder encoded = new StringBuilder();
        int inputLength = data.length;
        for (int i = 0; i < inputLength; i += 3) {
            int octetA = i < inputLength ? (data[i] & 0xFF) : 0;
            int octetB = (i + 1) < inputLength ? (data[i + 1] & 0xFF) : 0;
            int octetC = (i + 2) < inputLength ? (data[i + 2] & 0xFF) : 0;

            int triple = (octetA << 16) | (octetB << 8) | octetC;
            encoded.append(MODIFIED_BASE64_TABLE[(triple >> 18) & 0x3F]);
            encoded.append(MODIFIED_BASE64_TABLE[(triple >> 12) & 0x3F]);
            encoded.append((i + 1) < inputLength ? MODIFIED_BASE64_TABLE[(triple >> 6) & 0x3F] : '=');
            encoded.append((i + 2) < inputLength ? MODIFIED_BASE64_TABLE[triple & 0x3F] : '=');
        }
        return encoded.toString();
    }

    public static byte[] modifiedBase64Decode(String data) {
        int inputLength = data.length();
        if (inputLength % 4 != 0) return null;

        int padding = 0;
        if (data.endsWith("==")) padding = 2;
        else if (data.endsWith("=")) padding = 1;

        int outputLength = (inputLength / 4) * 3 - padding;
        byte[] decoded = new byte[outputLength];
        int j = 0;

        for (int i = 0; i < inputLength; i += 4) {
            int sextetA = data.charAt(i) == '=' ? 0 : BASE64_TABLE_REVERSE[data.charAt(i)];
            int sextetB = data.charAt(i + 1) == '=' ? 0 : BASE64_TABLE_REVERSE[data.charAt(i + 1)];
            int sextetC = data.charAt(i + 2) == '=' ? 0 : BASE64_TABLE_REVERSE[data.charAt(i + 2)];
            int sextetD = data.charAt(i + 3) == '=' ? 0 : BASE64_TABLE_REVERSE[data.charAt(i + 3)];

            int triple = (sextetA << 18) | (sextetB << 12) | (sextetC << 6) | sextetD;

            if (j < outputLength) decoded[j++] = (byte) ((triple >> 16) & 0xFF);
            if (j < outputLength) decoded[j++] = (byte) ((triple >> 8) & 0xFF);
            if (j < outputLength) decoded[j++] = (byte) (triple & 0xFF);
        }
        return decoded;
    }

    public static byte[] modifiedBase64DecodeUrl(String data) {

        String decodedUrlStr = URLDecoder.decode(data);
        return modifiedBase64Decode(decodedUrlStr);
    }
}


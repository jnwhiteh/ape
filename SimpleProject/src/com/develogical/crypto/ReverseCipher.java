package com.develogical.crypto;

public class ReverseCipher implements Cipher {
    public String encode(String text) {
        StringBuilder result = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
           result.append(chars[i]);

        }

        return result.toString();
    }

    public String decode(String text) {
        return encode(text);
    }
}

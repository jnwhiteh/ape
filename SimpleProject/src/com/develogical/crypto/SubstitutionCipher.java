package com.develogical.crypto;

import java.util.HashMap;
import java.util.Map;

public class SubstitutionCipher implements Cipher {

    Map<Character, Character> forwardAlphabet = new HashMap<Character, Character>();
    Map<Character, Character> reverseAlphabet = new HashMap<Character, Character>();

    public SubstitutionCipher() {
        this("abcdefghijklmnopqrstuvwxyz", "zebrascdfghijklmnopqtuvwxy");
    }

    SubstitutionCipher(String plaintextAlphabet, String ciphertextAlphabet) {
        for (int i = 0; i < plaintextAlphabet.length(); i++) {
            forwardAlphabet.put(plaintextAlphabet.charAt(i), ciphertextAlphabet.charAt(i));
            reverseAlphabet.put(ciphertextAlphabet.charAt(i), plaintextAlphabet.charAt(i));
        }
    }

    public String encode(String text) {
        return process(text, new Encoding());
    }

    public String decode(String text) {
        return process(text, new Decoding());
    }

    private String process(String text, Substitution substitution) {
        StringBuilder result = new StringBuilder();

        char[] chars = text.toCharArray();
        for (char c : chars) {
            result.append(substitution.applyTo(c));
        }

        return result.toString();
    }

    private interface Substitution {
        Character applyTo(char c);
    }

    private class Encoding implements Substitution {
        public Character applyTo(char c) {
            if (forwardAlphabet.containsKey(c)) {
                return forwardAlphabet.get(c);
            } else {
                return c;
            }
        }
    }

    private class Decoding implements Substitution {
        public Character applyTo(char c) {
            if (reverseAlphabet.containsValue(c)) {
                return reverseAlphabet.get(c);
            } else {
                return c;
            }
        }
    }
}

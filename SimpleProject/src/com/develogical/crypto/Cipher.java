package com.develogical.crypto;

public interface Cipher {
    String encode(String text);

    String decode(String text);
}

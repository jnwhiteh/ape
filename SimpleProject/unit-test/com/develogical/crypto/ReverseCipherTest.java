package com.develogical.crypto;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ReverseCipherTest {

    @Test
    public void encodesStringReversingLetters() {
        String result = new ReverseCipher().encode("the quick brown fox");
        assertThat(result, is("xof nworb kciuq eht"));
    }
    

    @Test
    public void decodesStringSubstitutingLetters() {
        String result = new ReverseCipher().decode("xof nworb kciuq eht");
        assertThat(result, is("the quick brown fox"));
    }
}

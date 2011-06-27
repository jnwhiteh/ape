package com.develogical.crypto;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class EncryptTest {

    @Test
    public void basicReverse() {
        assertThat(new Encrypt().run("-reverse", "abcd"), is("dcba"));
    }

    @Test
    public void basicSubstitute() {
        assertThat(new Encrypt().run("-substitute", "flee at once"), is("siaa zq lkba"));
    }

    @Test
    public void decodeSubstitute() {
        assertThat(new Encrypt().run("-decode", "-substitute", "siaa zq lkba"), is("flee at once"));
    }

}

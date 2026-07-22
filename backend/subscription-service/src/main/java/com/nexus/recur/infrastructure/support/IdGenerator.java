package com.nexus.recur.infrastructure.support;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class IdGenerator {
    private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private final SecureRandom random = new SecureRandom();

    public String next(String prefix) {
        char[] value = new char[16];
        for (int i = 0; i < value.length; i++) {
            value[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        }
        return prefix + "_" + new String(value);
    }
}

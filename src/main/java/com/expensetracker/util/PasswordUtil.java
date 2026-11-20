package com.expensetracker.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class for hashing and validating user passwords using salted SHA-256.
 */
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hashes a plain-text password using SHA-256 with a unique cryptographic salt.
     * Output format: <16-byte-salt-hex>:<sha256-hash-hex>
     */
    public static String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltHex = bytesToHex(salt);

        String hashHex = computeHash(saltHex, password);
        return saltHex + ":" + hashHex;
    }

    /**
     * Verifies whether a candidate plain-text password matches the stored salted hash.
     */
    public static boolean checkPassword(String candidatePassword, String storedHash) {
        if (candidatePassword == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }

        String[] parts = storedHash.split(":", 2);
        String saltHex = parts[0];
        String expectedHash = parts[1];

        String actualHash = computeHash(saltHex, candidatePassword);
        return MessageDigest.isEqual(
                actualHash.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String computeHash(String saltHex, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(saltHex.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

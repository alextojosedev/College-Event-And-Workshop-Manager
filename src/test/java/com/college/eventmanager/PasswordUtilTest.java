package com.college.eventmanager;

import com.college.eventmanager.service.PasswordUtil;

/**
 * Unit tests for PasswordUtil hashing and verification.
 */
public class PasswordUtilTest {

    public static void testPasswordHashing() {
        String raw = "student123";
        String hash = PasswordUtil.hashPassword(raw);

        assertNotNull(hash, "Hash should not be null");
        assertEquals(64, hash.length(), "SHA-256 hash length must be 64 characters");

        // Verify deterministic hashing
        String hash2 = PasswordUtil.hashPassword(raw);
        assertEquals(hash, hash2, "Hashing same password should produce identical hash");
    }

    public static void testPasswordVerification() {
        String raw = "admin123";
        String hash = PasswordUtil.hashPassword(raw);

        assertTrue(PasswordUtil.verifyPassword(raw, hash), "Password verification should return true for correct password");
        assertFalse(PasswordUtil.verifyPassword("wrongpass", hash), "Password verification should return false for incorrect password");
        assertFalse(PasswordUtil.verifyPassword(null, hash), "Password verification should return false for null password");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("Test Failed: " + message);
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError("Test Failed: " + message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Test Failed: " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) throw new AssertionError("Test Failed: " + message);
    }
}

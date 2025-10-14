package edu.vsu.lms.util;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class Passwords {

    private static final String SPECIALS = "!@#$%^&*";

    public static boolean isStrong(String p) {
        if (p == null || p.length() < 6) return false;
        boolean hasUpper=false, hasLower=false, hasDigit=false, hasSpecial=false;
        for (char c : p.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (SPECIALS.indexOf(c) >= 0) hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public static String hash(String p) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(p.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}

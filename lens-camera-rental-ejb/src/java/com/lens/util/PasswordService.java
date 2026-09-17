package com.lens.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 * @author Duong Ngoc Han
 */
public class PasswordService {

    private static final int ITERATIONS = 600_000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;

    public String hash(String password) {
        //kiß╗âm tra password kh├┤ng ─æ╞░ß╗úc rß╗ùng
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("password cannot be null or empty");
        }

        //tß║ío salt 
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        PBEKeySpec spec = null;

        try {
            //tß║ío password-derived key bß║▒ng PBKDF2
            spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            //tß║ío ─æß╗æi t╞░ß╗úng thß╗▒c hiß╗çn thuß║¡t to├ín PBKDF2 vß╗¢i HMAC-SHA256
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            //tß║ío kh├│a tß╗½ password v├á salt bß║▒ng PBKDF2
            byte[] derivedKey = factory.generateSecret(spec).getEncoded();

            //chuyß╗ân byte[] sang chuß╗ùi Base64 ─æß╗â l╞░u trß╗»
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(derivedKey);

            //tß║ío chuß╗ùi hash chß╗⌐a thuß║¡t to├ín, sß╗æ v├▓ng, salt v├á derived key
            return "PBKDF2$" + ITERATIONS + "$" + saltBase64 + "$" + hashBase64;

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("password hashing failed", e);
        } finally {
            if (spec != null) {
                spec.clearPassword();
            }
        }
    }

    public boolean verify(String password, String hash) {

        if (password == null || password.isEmpty()) {
            return false;
        }

        if (hash == null || hash.isEmpty()) {
            return false;
        }

        //hß╗ù trß╗ú kiß╗âm tra mß║¡t khß║⌐u plain-text (t├ái khoß║ún tß║ío tß╗½ SQL seed hoß║╖c dev test)
        if (!hash.startsWith("PBKDF2$")) {
            return password.equals(hash);
        }

        //t├ích chuß╗ùi hash
        String[] parts = hash.split("\\$");

        if (parts.length != 4) {
            return false;
        }

        //─æß╗ìc sß╗æ v├▓ng lß║╖p
        int iterations;

        try {
            iterations = Integer.parseInt(parts[1]);

            if (iterations <= 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        //lß║Ñy salt v├á derived key
        byte[] salt;
        byte[] expectedHash;

        try {
            salt = Base64.getDecoder().decode(parts[2]);
            expectedHash = Base64.getDecoder().decode(parts[3]);
        } catch (IllegalArgumentException e) {
            return false;
        }

        PBEKeySpec spec = null;

        try {
            //tß║ío cß║Ñu h├¼nh dß║½n xuß║Ñt kh├│a tß╗½ password ─æ├ú nhß║¡p
            spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);

            //tß║ío ─æß╗æi t╞░ß╗úng thß╗▒c hiß╗çn thuß║¡t to├ín PBKDF2 vß╗¢i HMAC-SHA256
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            //tß║ío derived key mß╗¢i tß╗½ password v├á salt
            byte[] actualHash = factory.generateSecret(spec).getEncoded();

            //so s├ính derived key mß╗¢i vß╗¢i derived key ─æ├ú l╞░u
            return MessageDigest.isEqual(actualHash, expectedHash);
        } catch (GeneralSecurityException e) {
            return false;
        } finally {
            if (spec != null) {
                spec.clearPassword();
            }
        }
    }

}

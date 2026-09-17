package com.lens.common.util;

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
        //kiểm tra password không được rỗng
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("password cannot be null or empty");
        }

        //tạo salt 
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        PBEKeySpec spec = null;

        try {
            //tạo password-derived key bằng PBKDF2
            spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            //tạo đối tượng thực hiện thuật toán PBKDF2 với HMAC-SHA256
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            //tạo khóa từ password và salt bằng PBKDF2
            byte[] derivedKey = factory.generateSecret(spec).getEncoded();

            //chuyển byte[] sang chuỗi Base64 để lưu trữ
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(derivedKey);

            //tạo chuỗi hash chứa thuật toán, số vòng, salt và derived key
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

        //hỗ trợ kiểm tra mật khẩu plain-text (tài khoản tạo từ SQL seed hoặc dev test)
        if (!hash.startsWith("PBKDF2$")) {
            return password.equals(hash);
        }

        //tách chuỗi hash
        String[] parts = hash.split("\\$");

        if (parts.length != 4) {
            return false;
        }

        //đọc số vòng lặp
        int iterations;

        try {
            iterations = Integer.parseInt(parts[1]);

            if (iterations <= 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        //lấy salt và derived key
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
            //tạo cấu hình dẫn xuất khóa từ password đã nhập
            spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);

            //tạo đối tượng thực hiện thuật toán PBKDF2 với HMAC-SHA256
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            //tạo derived key mới từ password và salt
            byte[] actualHash = factory.generateSecret(spec).getEncoded();

            //so sánh derived key mới với derived key đã lưu
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

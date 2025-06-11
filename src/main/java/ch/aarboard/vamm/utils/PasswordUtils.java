package ch.aarboard.vamm.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification.
 * Provides methods to hash passwords using different schemes (SSHA, SHA, MD5)
 * and verify them against plain text passwords.
 *
 * @author fokklz
 */
public class PasswordUtils {

    /**
     * Enum representing different password hashing schemes.
     * Each scheme has a prefix that is used to identify the hashing method.
     */
    public enum PasswordScheme {
        PLAIN("{PLAIN}"),
        SSHA("{SSHA}"),
        SHA("{SHA}"),
        MD5("{MD5}");

        private final String prefix;

        PasswordScheme(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    /**
     * Generate plain text password
     * @param plainPassword the plain text password
     * @return the hashed password
     */
    public static String hashPasswordSsha(String plainPassword) {
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[8];
            random.nextBytes(salt);

            // Create SHA-1 hash of password + salt
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(plainPassword.getBytes(StandardCharsets.UTF_8));
            md.update(salt);
            byte[] hash = md.digest();

            // Combine hash + salt and encode in base64
            byte[] hashPlusSalt = new byte[hash.length + salt.length];
            System.arraycopy(hash, 0, hashPlusSalt, 0, hash.length);
            System.arraycopy(salt, 0, hashPlusSalt, hash.length, salt.length);

            return PasswordScheme.SSHA.getPrefix() + Base64.getEncoder().encodeToString(hashPlusSalt);

        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password with SSHA", e);
        }
    }

    /**
     * Generate SHA-1 hash
     * @param plainPassword the plain text password
     * @return the hashed password
     */
    public static String hashPasswordSha(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return PasswordScheme.SHA.getPrefix() + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password with SHA", e);
        }
    }

    /**
     * Generate MD5 hash (not recommended for security)
     * @param plainPassword the plain text password
     * @return the hashed password
     */
    public static String hashPasswordMd5(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return PasswordScheme.MD5.getPrefix() + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password with MD5", e);
        }
    }

    /**
     * Verify a plain text password against a stored hashed password.
     * Determines the hashing scheme used and calls the appropriate verification method.
     *
     * @param userPassword the stored hashed password
     * @param plainPassword the plain text password to verify
     * @return true if the passwords match, false otherwise
     */
    public static boolean verifyPassword(String userPassword, String plainPassword) {
        if (userPassword == null || plainPassword == null) {
            return false;
        }

        // Determine the scheme used
        if (userPassword.startsWith(PasswordScheme.SSHA.getPrefix())) {
            return verifyPasswordSsha(userPassword, plainPassword);
        } else if (userPassword.startsWith(PasswordScheme.SHA.getPrefix())) {
            return verifyPasswordSha(userPassword, plainPassword);
        } else if (userPassword.startsWith(PasswordScheme.MD5.getPrefix())) {
            return verifyPasswordMd5(userPassword, plainPassword);
        } else if (userPassword.startsWith(PasswordScheme.PLAIN.getPrefix())) {
            String storedPassword = userPassword.substring(PasswordScheme.PLAIN.getPrefix().length());
            return storedPassword.equals(plainPassword);
        }

        return false;
    }

    /**
     * Verify a plain text password against a stored hashed password. for SSHA scheme.
     *
     * @param userPassword the stored hashed password
     * @param plainPassword the plain text password to verify
     * @return true if the passwords match, false otherwise
     */
    public static boolean verifyPasswordSsha(String userPassword, String plainPassword) {
        try {
            String hashString = userPassword.substring(PasswordScheme.SSHA.getPrefix().length());
            byte[] hashPlusSalt = Base64.getDecoder().decode(hashString);

            // Extract salt (last 8 bytes)
            byte[] salt = new byte[8];
            System.arraycopy(hashPlusSalt, hashPlusSalt.length - 8, salt, 0, 8);

            // Extract hash (first bytes)
            byte[] storedHash = new byte[hashPlusSalt.length - 8];
            System.arraycopy(hashPlusSalt, 0, storedHash, 0, storedHash.length);

            // Calculate hash of provided password with stored salt
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(plainPassword.getBytes(StandardCharsets.UTF_8));
            md.update(salt);
            byte[] calculatedHash = md.digest();

            return MessageDigest.isEqual(storedHash, calculatedHash);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verify a plain text password against a stored hashed password for SHA scheme.
     *
     * @param userPassword the stored hashed password
     * @param plainPassword the plain text password to verify
     * @return true if the passwords match, false otherwise
     */
    public static boolean verifyPasswordSha(String userPassword, String plainPassword) {
        try {
            String hashString = userPassword.substring(PasswordScheme.SHA.getPrefix().length());
            byte[] storedHash = Base64.getDecoder().decode(hashString);

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] calculatedHash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));

            return MessageDigest.isEqual(storedHash, calculatedHash);
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Verify a plain text password against a stored hashed password for MD5 scheme.
     *
     * @param userPassword the stored hashed password
     * @param plainPassword the plain text password to verify
     * @return true if the passwords match, false otherwise
     */
    public static boolean verifyPasswordMd5(String userPassword, String plainPassword) {
        try {
            String hashString = userPassword.substring(PasswordScheme.MD5.getPrefix().length());
            byte[] storedHash = Base64.getDecoder().decode(hashString);

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] calculatedHash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));

            return MessageDigest.isEqual(storedHash, calculatedHash);
        } catch (Exception e) {
            return false;
        }
    }

}

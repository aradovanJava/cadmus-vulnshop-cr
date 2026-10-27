package eu.cadmus.vulnshop.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Random;

/**
 * ============================================================================
 *  A04:2025 - CRYPTOGRAPHIC FAILURES
 * ============================================================================
 *
 * Four independent failures in one small class. Note that each one is a
 * DECISION, not a typo - which is the point node N1 makes about root causes:
 *
 *   1. A broken hash function chosen for password storage.
 *   2. No salt, so identical passwords produce identical hashes.
 *   3. A hardcoded symmetric key, in source control, in ECB mode.
 *   4. A non-cryptographic PRNG used to mint security tokens.
 *
 * Remediation targets for CR2 are listed in docs/FIXES.md.
 */
@Service
public class CryptoService {

    /**
     * A04 - hardcoded key committed to the repository. Rotating it now requires
     * a code change, a build and a deploy, which is why it has never been rotated.
     * 16 bytes so that AES accepts it.
     */
    private static final String SECRET_KEY = "CadmusVulnShop16";

    /** A04 - java.util.Random is predictable; seeding it makes that worse. */
    private final Random tokenSource = new Random();

    /**
     * A04 - MD5, unsalted, no work factor. A collision-broken digest used for the
     * one job it is least suited to. Correct answer: BCrypt/Argon2 via
     * Spring Security's PasswordEncoder.
     */
    public String hashPassword(String plaintext) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(plaintext.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            // A10 - swallowed, then converted into a value the caller will treat as a hash.
            return "";
        }
    }

    /**
     * A04 - AES in ECB mode with a static key and no authentication tag.
     * Identical plaintext blocks produce identical ciphertext blocks.
     */
    public String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES"));
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * A04 + A07 - session and reset tokens minted from java.util.Random.
     * Predicting one lets an attacker predict the next.
     */
    public String newToken() {
        return Long.toHexString(tokenSource.nextLong());
    }

    /**
     * A06 - a four digit reset code, from the same weak source.
     * There are 10,000 possibilities and PasswordResetController has no rate limit.
     */
    public String newResetCode() {
        return String.format("%04d", tokenSource.nextInt(10000));
    }
}

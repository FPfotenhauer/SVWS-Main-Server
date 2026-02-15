package de.schultraeger.infrastructure.crypto;

import de.schultraeger.application.port.out.PasswordCipher;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

/**
 * AES-GCM implementation for SVWS password encryption.
 */
@ApplicationScoped
public class AesGcmPasswordCipher implements PasswordCipher {
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmPasswordCipher(@ConfigProperty(name = "svws.password.key") Optional<String> base64Key) {
        this.key = base64Key.filter(value -> !value.isBlank())
                .map(Base64.getDecoder()::decode)
                .map(bytes -> new SecretKeySpec(bytes, "AES"))
                .orElse(null);
    }

    @Override
    public String encrypt(String plainText) {
        ensureKeyConfigured();
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to encrypt password", ex);
        }
    }

    @Override
    public String decrypt(String encryptedText) {
        ensureKeyConfigured();
        try {
            String[] parts = encryptedText.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid encrypted format");
            }
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherBytes = Base64.getDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to decrypt password", ex);
        }
    }

    private void ensureKeyConfigured() {
        if (key == null) {
            throw new IllegalStateException("svws.password.key must be configured before encrypt/decrypt");
        }
    }
}

package de.schultraeger.application.port.out;

/**
 * Port for encrypting/decrypting SVWS credentials.
 */
public interface PasswordCipher {
    String encrypt(String plainText);

    String decrypt(String encryptedText);
}

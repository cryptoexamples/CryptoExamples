package com.cryptoexamples.java;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * All in one example for encryption and decryption of a file in one method;
 * Including
 * - random password generation,
 * - random salt generation,
 * - key derivation using PBKDF2 HMAC SHA-256,
 * - AES-256 authenticated encryption using GCM
 * - BASE64-encoding for the byte-arrays
 * - exception handling
 */
public class ExampleFileEncryptionInOneMethod {
  private static final Logger LOGGER = Logger.getLogger(ExampleFileEncryptionInOneMethod.class.getName());

  public static void main(String[] args) {
    String plainText = "Multiline text:";
    try {
      // GENERATE password
      KeyGenerator keyGen = KeyGenerator.getInstance("AES");
      // Needs unlimited strength policy files http://www.oracle.com/technetwork/java/javase/downloads
      keyGen.init(256);
      String password = Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());

      // GENERATE random salt
      final byte[] salt = new byte[12];
      SecureRandom random = SecureRandom.getInstanceStrong();
      random.nextBytes(salt);

      // DERIVE key (from password and salt)
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      // Needs unlimited strength policy files http://www.oracle.com/technetwork/java/javase/downloads
      KeySpec keyspec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
      SecretKey tmp = factory.generateSecret(keyspec);
      SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");

      // GENERATE random nonce (number used once)
      final byte[] nonce = new byte[32];
      random.nextBytes(nonce);

      // SET UP CIPHER for encryption
      Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
      GCMParameterSpec spec = new GCMParameterSpec(16 * 8, nonce);
      cipher.init(Cipher.ENCRYPT_MODE, key, spec);

      //byte[] aad = "Additional authenticated not encrypted data".getBytes();
      //cipher.updateAAD(aad);

      // TODO store encryption parameters as authenticated data prepended to the file content

      // SET UP OUTPUT STREAM and write content of String
      try (
              FileOutputStream fileOutputStream = new FileOutputStream("encryptedFile.enc");
              CipherOutputStream encryptedOutputStream = new CipherOutputStream(fileOutputStream, cipher);
              InputStream stringInputStream = new ByteArrayInputStream(plainText.getBytes(StandardCharsets.UTF_8));
      ) {
        byte[] buffer = new byte[8192];
        while (stringInputStream.read(buffer) > 0) {
          encryptedOutputStream.write(buffer);
        }
      }

      // READ ENCRYPTED FILE
      StringBuilder stringBuilder = new StringBuilder();
      cipher.init(Cipher.DECRYPT_MODE, key, spec);
      //cipher.updateAAD(aad);
      try (
              FileInputStream fileInputStream = new FileInputStream("encryptedFile.enc");
              CipherInputStream cipherInputStream = new CipherInputStream(fileInputStream, cipher);
      ) {
        byte[] buffer = new byte[8192];
        while (cipherInputStream.read(buffer) > 0) {
          stringBuilder.append(new String(buffer, StandardCharsets.UTF_8));
        }
      }
      // TODO trim() should not be needed!
      String decryptedCipherText = stringBuilder.toString().trim();
      LOGGER.log(Level.INFO, decryptedCipherText);

      LOGGER.log(Level.INFO,
              () -> String.format("Decrypted and original plain text are the same: %s",
                      decryptedCipherText.compareTo(plainText) == 0 ? "true" : "false")
      );
    } catch (NoSuchAlgorithmException |
            NoSuchPaddingException |
            InvalidKeyException |
            InvalidParameterException |
            InvalidAlgorithmParameterException |
            InvalidKeySpecException |
            IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

}

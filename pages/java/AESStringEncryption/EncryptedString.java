package com.cryptoexamples.java;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Encapsulating class for encrypting and decrypting strings
 * Including
 * - random password generation,
 * - random salt generation,
 * - key derivation using PBKDF2 HMAC SHA-256,
 * - AES-256 authenticated encryption using GCM
 * - BASE64-encoding for the byte-arrays
 */
public class EncryptedString implements Serializable {

  /* 128, 120, 112, 104, or 96 @see NIST Special Publication 800-38D*/
  private static final int DEFAULT_GCM_AUTHENTICATION_TAG_SIZE_BITS = 128;

  private static final int DEFAULT_GCM_IV_NONCE_SIZE_BYTES = 12;
  private static final int DEFAULT_PBKDF2_ITERATIONS = 65536;
  private static final int DEFAULT_PBKDF2_SALT_SIZE_BYTES = 32;

  /* @see https://www.keylength.com/ */
  private static final int DEFAULT_AES_KEY_LENGTH_BITS = 256;
  private static final String DEFAULT_CIPHER = "AES";
  private static final String DEFAULT_CIPHERSCHEME = "AES/GCM/PKCS5Padding";
  private static final String DEFAULT_PBKDF2_SCHEME = "PBKDF2WithHmacSHA256";

  private int gcmAuthenticationTagSizeBits = DEFAULT_GCM_AUTHENTICATION_TAG_SIZE_BITS;
  private int gcmIvNonceSizeBytes = DEFAULT_GCM_IV_NONCE_SIZE_BYTES;
  private int pbkdf2Iterations = DEFAULT_PBKDF2_ITERATIONS;
  private int pbkdf2SaltSizeBytes = DEFAULT_PBKDF2_SALT_SIZE_BYTES;
  private int aesKeyLengthBits = DEFAULT_AES_KEY_LENGTH_BITS;
  private String cipher = DEFAULT_CIPHER;
  private String cipherscheme = DEFAULT_CIPHERSCHEME;
  private String pbkdf2Scheme = DEFAULT_PBKDF2_SCHEME;

  private byte[] nonce;
  private byte[] salt;
  private String cipherText;

  /**
   * Creates a new com.cryptoexamples.java.EncryptedString object based on cipherText, nonce and salt.
   *
   * @param cipherText encrypted plaintext (generated from encrypt)
   * @param nonce      byte array, number used once (random) see gcmIvNonceSizeBytes
   * @param salt       random byte array to prevent rainbow table attacks on password lists
   */
  public EncryptedString(String cipherText, byte[] nonce, byte[] salt) {
    this.cipherText = cipherText;
    this.nonce = nonce;
    this.salt = salt;
  }

  /**
   * Initializes this com.cryptoexamples.java.EncryptedString object with the provided parameters
   *
   * @param cipher
   * @param cipherscheme
   * @param gcmAuthenticationTagSizeBits
   * @param gcmIvNonceSizeBytes
   * @param pbkdf2Iterations
   * @param pbkdf2SaltSizeBytes
   * @param aesKeyLengthBits
   * @param pbkdf2Scheme
   */
  private EncryptedString(String cipherText, byte[] nonce, byte[] salt, String cipher, String cipherscheme, int gcmAuthenticationTagSizeBits, int gcmIvNonceSizeBytes, int pbkdf2Iterations, int pbkdf2SaltSizeBytes, int aesKeyLengthBits, String pbkdf2Scheme) {
    this.cipherText = cipherText;
    this.nonce = nonce;
    this.salt = salt;

    this.cipher = cipher;
    this.cipherscheme = cipherscheme;
    this.gcmAuthenticationTagSizeBits = gcmAuthenticationTagSizeBits;
    this.gcmIvNonceSizeBytes = gcmIvNonceSizeBytes;
    this.pbkdf2Iterations = pbkdf2Iterations;
    this.pbkdf2SaltSizeBytes = pbkdf2SaltSizeBytes;
    this.aesKeyLengthBits = aesKeyLengthBits;
    this.pbkdf2Scheme = pbkdf2Scheme;
  }

  /**
   * Creates a new empty com.cryptoexamples.java.EncryptedString object
   */
  public EncryptedString() {
    // uses default parameters, see initialization at the beginning.
  }

  /**
   * Generates a randomly filled byte array
   *
   * @param sizeInBytes length of the array in bytes
   * @return byte array containing random values
   * @throws NoSuchAlgorithmException
   */
  private static byte[] generateRandomArry(int sizeInBytes) throws NoSuchAlgorithmException {
    /* generate random salt */
    final byte[] salt = new byte[sizeInBytes];
    SecureRandom random = SecureRandom.getInstanceStrong();
    random.nextBytes(salt);
    return salt;
  }

  /**
   * Generates a random password.
   *
   * @param sizeInBytes length of the password in byte
   * @return Base64 encoded string with a random password
   * @throws NoSuchAlgorithmException
   */
  public static String generatePassword(int sizeInBytes) throws NoSuchAlgorithmException {
    return Base64.getEncoder().encodeToString(generateRandomArry(sizeInBytes));
  }

  private byte[] getNonce() {
    return this.nonce;
  }

  private byte[] getSalt() {
    return this.salt;
  }

  private String getCipherText() {
    return this.cipherText;
  }

  /**
   * Encrypts the provided plainText using the provided password.
   *
   * @param plainText plaintext that should be encrypted
   * @param password  password which is used to generate the key
   * @return new com.cryptoexamples.java.EncryptedString object
   * @throws GeneralSecurityException
   */
  public EncryptedString encrypt(String plainText, String password) throws GeneralSecurityException {
    /* Derive the key*/
    SecretKeyFactory factory = SecretKeyFactory.getInstance(pbkdf2Scheme);
    byte[] newSalt = generateRandomArry(pbkdf2SaltSizeBytes);
    KeySpec keyspec = new PBEKeySpec(password.toCharArray(), newSalt, pbkdf2Iterations, aesKeyLengthBits);
    SecretKey tmp = factory.generateSecret(keyspec);
    SecretKey key = new SecretKeySpec(tmp.getEncoded(), cipher);

    Cipher myCipher = Cipher.getInstance(cipherscheme);
    byte[] newNonce = generateRandomArry(gcmIvNonceSizeBytes);
    GCMParameterSpec spec = new GCMParameterSpec(gcmAuthenticationTagSizeBits, newNonce);
    myCipher.init(Cipher.ENCRYPT_MODE, key, spec);

    byte[] byteCipher = myCipher.doFinal(plainText.getBytes());

    return new EncryptedString(new String(Base64.getEncoder().encode(byteCipher)), newNonce, newSalt, this.cipher, cipherscheme, gcmAuthenticationTagSizeBits, gcmIvNonceSizeBytes, pbkdf2Iterations, pbkdf2SaltSizeBytes, aesKeyLengthBits, pbkdf2Scheme);
  }

  /**
   * Decrypts the cipherText using the provided password.
   *
   * @param password password which is used to generate the key
   * @return plaintext
   * @throws GeneralSecurityException
   */
  public String decrypt(String password) throws GeneralSecurityException {
      /* Derive the key*/
    SecretKeyFactory factory = SecretKeyFactory.getInstance(pbkdf2Scheme);
    // Needs unlimited strength policy files http://www.oracle.com/technetwork/java/javase/downloads
    KeySpec keyspec = new PBEKeySpec(password.toCharArray(), getSalt(), pbkdf2Iterations, aesKeyLengthBits);
    SecretKey tmp = factory.generateSecret(keyspec);
    SecretKey key = new SecretKeySpec(tmp.getEncoded(), cipher);

    Cipher myCipher = Cipher.getInstance(cipherscheme);
    GCMParameterSpec spec = new GCMParameterSpec(gcmAuthenticationTagSizeBits, getNonce());

    myCipher.init(Cipher.DECRYPT_MODE, key, spec);

    byte[] decryptedCipher = myCipher.doFinal(Base64.getDecoder().decode(getCipherText()));
    return new String(decryptedCipher);
  }
}


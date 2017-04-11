import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

// TODO check if KeyStore should be used

/**
 * Object oriented example for encryption and decryption of a string;
 * Including
 * - random password generation,
 * - random salt generation,
 * - key derivation using PBKDF2 HMAC SHA-256,
 * - AES-256 authenticated encryption using GCM
 * - BASE64-encoding for the byte-arrays
 * - exception handling
 */
public class AESStringEncryptionOO {

  final static int GCM_AUTHENTICATION_TAG_SIZE_BITS = 128;
  final static int GCM_IV_NONCE_SIZE_BYTES = 12;
  final static int PBKDF2_ITERATIONS = 65536;
  final static int PBKDF2_SALT_SIZE_BYTES = 32;
  final static int AES_KEY_LENGTH_BITS = 256;

  public static void main(String[] args) {
    String plainText = "Text that is going to be sent over an insecure channel and must be encrypted at all costs!";
    //String password = "givenPassword";
    try {
      String password = generatePassword();
      byte[] salt = generateSalt();
      byte[] nonce = generateNonce();
      String cipherText = encrypt(plainText, password, salt, nonce);
      String decryptedCipherText = decrypt(cipherText, password, salt, nonce);

      boolean encryptionSuccessful = decryptedCipherText.compareTo(plainText) == 0;
      System.out.print("Decrypted and original plain text are the same: " + (encryptionSuccessful ? "true" : "false"));
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
      System.out.println("Error: " + e.getLocalizedMessage());
      e.printStackTrace();
    }
  }

  public static String encrypt(String plainText, String password, final byte[] salt, final byte[] nonce) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
    /* Derive the key*/
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    // Needs unlimited strength policy files http://www.oracle.com/technetwork/java/javase/downloads
    KeySpec keyspec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_LENGTH_BITS);
    SecretKey tmp = factory.generateSecret(keyspec);
    SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");

    Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");

    GCMParameterSpec spec = new GCMParameterSpec(GCM_AUTHENTICATION_TAG_SIZE_BITS, nonce);
    cipher.init(Cipher.ENCRYPT_MODE, key, spec);

    byte[] byteCipher = cipher.doFinal(plainText.getBytes());

    return new String(Base64.getEncoder().encode(byteCipher));
  }

  public static String decrypt(String cipherText, String password, byte[] salt, byte[] nonce) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    /* Derive the key*/
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    // Needs unlimited strength policy files http://www.oracle.com/technetwork/java/javase/downloads
    KeySpec keyspec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_LENGTH_BITS);
    SecretKey tmp = factory.generateSecret(keyspec);
    SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");

    Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
    GCMParameterSpec spec = new GCMParameterSpec(GCM_AUTHENTICATION_TAG_SIZE_BITS, nonce);

    cipher.init(Cipher.DECRYPT_MODE, key, spec);

    byte[] decryptedCipher = cipher.doFinal(Base64.getDecoder().decode(cipherText));
    return new String(decryptedCipher);
  }

  public static String generatePassword() throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    // Needs unlimited strength policy files http://www.oracle.com/technetwork/java/javase/downloads
    keyGen.init(AES_KEY_LENGTH_BITS);
    return Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());
  }

  public static byte[] generateSalt() throws NoSuchAlgorithmException {
    /* generate random salt */
    final byte[] salt = new byte[PBKDF2_SALT_SIZE_BYTES];
    SecureRandom random = SecureRandom.getInstanceStrong();
    random.nextBytes(salt);
    return salt;
  }

  public static byte[] generateNonce() throws NoSuchAlgorithmException {
    SecureRandom random = SecureRandom.getInstanceStrong();
    final byte[] nonce = new byte[GCM_IV_NONCE_SIZE_BYTES];
    random.nextBytes(nonce);
    return nonce;
  }

}

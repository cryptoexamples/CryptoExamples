import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

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

// TODO check if byte arrays need to be replaced by something like byteSource (deletion of content)

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
    KeySpec keyspec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
    SecretKey tmp = factory.generateSecret(keyspec);
    SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");

    Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");

    GCMParameterSpec spec = new GCMParameterSpec(16 * 8, nonce);
    cipher.init(Cipher.ENCRYPT_MODE, key, spec);

    byte[] byteCipher = cipher.doFinal(plainText.getBytes());

    Encoder b64Encoder = Base64.getEncoder();
    return new String(b64Encoder.encode(byteCipher));
  }

  public static String decrypt(String cipherText, String password, byte[] salt, byte[] nonce) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    /* Derive the key*/
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    // Needs unlimited strength policy files http://www.oracle.com/technetwork/java/javase/downloads
    KeySpec keyspec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
    SecretKey tmp = factory.generateSecret(keyspec);
    SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");

    Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
    GCMParameterSpec spec = new GCMParameterSpec(16 * 8, nonce);

    Decoder b64Decoder = Base64.getDecoder();

    cipher.init(Cipher.DECRYPT_MODE, key, spec);

    byte[] decryptedCipher = cipher.doFinal(b64Decoder.decode(cipherText));
    return new String(decryptedCipher);
  }

  public static String generatePassword() throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    // Needs unlimited strength policy files http://www.oracle.com/technetwork/java/javase/downloads
    keyGen.init(256);
    return Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());
  }

  public static byte[] generateSalt() throws NoSuchAlgorithmException {
    /* generate random salt */
    final byte[] salt = new byte[12];
    SecureRandom random = SecureRandom.getInstanceStrong();
    random.nextBytes(salt);
    return salt;
  }

  public static byte[] generateNonce() throws NoSuchAlgorithmException {
    SecureRandom random = SecureRandom.getInstanceStrong();
    final byte[] nonce = new byte[12];
    random.nextBytes(nonce);
    return nonce;
  }

}

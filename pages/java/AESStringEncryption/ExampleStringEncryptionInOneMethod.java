import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * All in one example for encryption and decryption of a string in one method;
 * Including
 * - random password generation,
 * - random salt generation,
 * - key derivation using PBKDF2 HMAC SHA-256,
 * - AES-256 authenticated encryption using GCM
 * - BASE64-encoding for the byte-arrays
 * - exception handling
 */
public class ExampleStringEncryptionInOneMethod {
  private static final Logger LOGGER = Logger.getLogger( ExampleStringEncryptionInOneMethod.class.getName() );
  public static void main(String[] args) {
    String plainText = "Text that is going to be sent over an insecure channel and must be encrypted at all costs!";
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

      // ENCRYPTION
      Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
      GCMParameterSpec spec = new GCMParameterSpec(16 * 8, nonce);
      cipher.init(Cipher.ENCRYPT_MODE, key, spec);

      //byte[] aad = "Additional authenticated not encrypted data".getBytes();
      //cipher.updateAAD(aad);

      byte[] byteCipher = cipher.doFinal(plainText.getBytes());
      // CONVERSION of raw bytes to BASE64 representation
      String cipherText = new String(Base64.getEncoder().encode(byteCipher));

      // DECRYPTION
      cipher.init(Cipher.DECRYPT_MODE, key, spec);
      //cipher.updateAAD(aad);
      byte[] decryptedCipher = cipher.doFinal(Base64.getDecoder().decode(cipherText));
      String decryptedCipherString = new String(decryptedCipher);

      LOGGER.info("INFORMATION: Decrypted and original plain text are the same: " + ((decryptedCipherString.compareTo(plainText))==0 ? "true" : "false"));
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidParameterException | InvalidAlgorithmParameterException | InvalidKeySpecException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

}

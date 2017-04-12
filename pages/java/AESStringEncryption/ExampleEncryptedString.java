import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class ExampleEncryptedString {
  private static final Logger LOGGER = Logger.getLogger( ExampleEncryptedString.class.getName() );
  public static void main(String[] args) {
    String plainText = "Text that is going to be sent over an insecure channel and must be encrypted at all costs!";

    try {
      // GENERATE a password (if a password exists, use that).
      String password = EncryptedString.generatePassword(32);

      // ENCRYPTION
      EncryptedString encryptedString = new EncryptedString().encrypt(plainText, password);

      // DECRYPTION
      String decryptedCipherText = encryptedString.decrypt(password);

      boolean encryptionSuccessful = decryptedCipherText.compareTo(plainText) == 0;
      System.out.print("Decrypted and original plain text are the same: " + (encryptionSuccessful ? "true" : "false"));
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

}

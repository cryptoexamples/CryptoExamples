import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

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
public class AESStringEncryptionOOEasy {

  public static void main(String[] args) {
    String plainText = "Text that is going to be sent over an insecure channel and must be encrypted at all costs!";
    //String password = "givenPassword";
    try {
      String password = EncryptedString.generatePassword(32);

      // ENCRYPTION
      EncryptedString encryptedString = new EncryptedString().encrypt(plainText, password);

      // DECRYPTION
      String decryptedCipherText = encryptedString.decrypt(password);

      boolean encryptionSuccessful = decryptedCipherText.compareTo(plainText) == 0;
      System.out.print("Decrypted and original plain text are the same: " + (encryptionSuccessful ? "true" : "false"));
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
      System.out.println("Error: " + e.getLocalizedMessage());
      e.printStackTrace();
    }
  }

}

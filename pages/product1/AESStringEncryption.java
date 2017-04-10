import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
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

public class AESStringEncryption {

  public static void main(String[] args) {
    String plainText = "Text that is going to be sent over an insecure channel and must be encrypted at all costs!";

    try {

      // generate password, if you have one save it in `password`
      KeyGenerator keyGen = KeyGenerator.getInstance("AES");
      // Needs unlimited strength policy files http://www.oracle.com/technetwork/java/javase/downloads
      keyGen.init(256);
      String password = Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());

      final byte[] salt = new byte[12];
      SecureRandom random = SecureRandom.getInstanceStrong();
      random.nextBytes(salt);
      /* Derive the key, given password and salt. */
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      // Needs unlimited strength policy files http://www.oracle.com/technetwork/java/javase/downloads
      KeySpec keyspec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
      SecretKey tmp = factory.generateSecret(keyspec);
      SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");

      Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
      final byte[] nonce = new byte[12];
      random.nextBytes(nonce);
      GCMParameterSpec spec = new GCMParameterSpec(16 * 8, nonce);
      cipher.init(Cipher.ENCRYPT_MODE, key, spec);

      //byte[] aad = "Whatever I like".getBytes();
      //cipher.updateAAD(aad);

      byte[] byteCipher = cipher.doFinal(plainText.getBytes());

      Encoder b64Encoder = Base64.getEncoder();
      String cipherText = new String(b64Encoder.encode(byteCipher));


      cipher.init(Cipher.DECRYPT_MODE, key, spec);
      //cipher.updateAAD(aad);
      byte[] decryptedCipher = cipher.doFinal(byteCipher);
      String decryptedCipherString = new String(decryptedCipher);

      System.out.println("Decrypted and original plain text are the same: " + ((decryptedCipherString.compareTo(plainText))==0 ? "true" : "false"));

    } catch (NoSuchAlgorithmException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      return;
    } catch (NoSuchPaddingException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      return;
    } catch (InvalidKeyException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      return;
    } catch (IllegalBlockSizeException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      return;
    } catch (BadPaddingException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      return;
    } catch (InvalidAlgorithmParameterException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    } catch (InvalidKeySpecException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

}

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
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
  public int GCM_AUTHENTICATION_TAG_SIZE_BITS = 128;
  public int GCM_IV_NONCE_SIZE_BYTES = 12;
  public int PBKDF2_ITERATIONS = 65536;
  public int PBKDF2_SALT_SIZE_BYTES = 32;
  public int AES_KEY_LENGTH_BITS = 256;
  public String CIPHER = "AES";
  public String CIPHERSCHEME = "AES/GCM/PKCS5Padding";

  private byte[] nonce;
  private byte[] salt;
  private String cipherText;

  /**
   * Creates a new EncryptedString object based on cipherText, nonce and salt.
   * @param cipherText encrypted plaintext (generated from encrypt)
   * @param nonce byte array, number used once (random) see GCM_IV_NONCE_SIZE_BYTES
   * @param salt random byte array to prevent rainbow table attacks on password lists
   */
  public EncryptedString(String cipherText, byte[] nonce, byte[] salt) {
    this.cipherText = cipherText;
    this.nonce = nonce;
    this.salt = salt;
  }

  /**
   * Creates a new empty EncryptedString object
   */
  public EncryptedString() {

  }

  public byte[] getNonce() {
    return this.nonce;
  }

  public byte[] getSalt() {
    return this.salt;
  }

  public String getCipherText() {
    return this.cipherText;
  }

  /**
   * Generates a randomly filled byte array
   * @param sizeInBytes length of the array in bytes
   * @return byte array containing random values
   * @throws NoSuchAlgorithmException
   */
  public static byte[] generateRandomArry(int sizeInBytes) throws NoSuchAlgorithmException {
    /* generate random salt */
    final byte[] salt = new byte[sizeInBytes];
    SecureRandom random = SecureRandom.getInstanceStrong();
    random.nextBytes(salt);
    return salt;
  }

  /**
   * Encrypts the provided plainText using the provided password.
   * @param plainText plaintext that should be encrypted
   * @param password password which is used to generate the key
   * @return new EncryptedString object
   * @throws BadPaddingException
   * @throws IllegalBlockSizeException
   * @throws InvalidAlgorithmParameterException
   * @throws InvalidKeyException
   * @throws NoSuchPaddingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  public EncryptedString encrypt(String plainText, String password) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
    /* Derive the key*/
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    // Needs unlimited strength policy files http://www.oracle.com/technetwork/java/javase/downloads
    EncryptedString encryptedString = new EncryptedString();
    byte[] salt = generateRandomArry(PBKDF2_SALT_SIZE_BYTES);
    KeySpec keyspec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_LENGTH_BITS);
    SecretKey tmp = factory.generateSecret(keyspec);
    SecretKey key = new SecretKeySpec(tmp.getEncoded(), CIPHER);

    Cipher cipher = Cipher.getInstance(CIPHERSCHEME);
    byte[] nonce = generateRandomArry(GCM_IV_NONCE_SIZE_BYTES);
    GCMParameterSpec spec = new GCMParameterSpec(GCM_AUTHENTICATION_TAG_SIZE_BITS, nonce);
    cipher.init(Cipher.ENCRYPT_MODE, key, spec);

    byte[] byteCipher = cipher.doFinal(plainText.getBytes());

    return new EncryptedString(new String(Base64.getEncoder().encode(byteCipher)), nonce, salt);
  }

  /**
   * Decrypts the cipherText using the provided password.
   * @param password password which is used to generate the key
   * @return plaintext
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   * @throws NoSuchPaddingException
   * @throws InvalidAlgorithmParameterException
   * @throws InvalidKeyException
   * @throws BadPaddingException
   * @throws IllegalBlockSizeException
   */
  public String decrypt(String password) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    /* Derive the key*/
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    // Needs unlimited strength policy files http://www.oracle.com/technetwork/java/javase/downloads
    KeySpec keyspec = new PBEKeySpec(password.toCharArray(), getSalt(), PBKDF2_ITERATIONS, AES_KEY_LENGTH_BITS);
    SecretKey tmp = factory.generateSecret(keyspec);
    SecretKey key = new SecretKeySpec(tmp.getEncoded(), CIPHER);

    Cipher cipher = Cipher.getInstance(CIPHERSCHEME);
    GCMParameterSpec spec = new GCMParameterSpec(GCM_AUTHENTICATION_TAG_SIZE_BITS, getNonce());

    cipher.init(Cipher.DECRYPT_MODE, key, spec);

    byte[] decryptedCipher = cipher.doFinal(Base64.getDecoder().decode(getCipherText()));
    return new String(decryptedCipher);
  }

  /**
   * Generates a random password.
   * @param sizeInBytes length of the password in byte
   * @return Base64 encoded string with a random password
   * @throws NoSuchAlgorithmException
   */
  public static String generatePassword(int sizeInBytes) throws NoSuchAlgorithmException {
    return Base64.getEncoder().encodeToString(generateRandomArry(sizeInBytes));
  }
}


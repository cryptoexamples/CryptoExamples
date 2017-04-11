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

  /* 128, 120, 112, 104, or 96 @see NIST Special Publication 800-38D*/
  private final int DEFAULT_GCM_AUTHENTICATION_TAG_SIZE_BITS = 128; 
  private final int DEFAULT_GCM_IV_NONCE_SIZE_BYTES = 12;
  private final int DEFAULT_PBKDF2_ITERATIONS = 65536;
  private final int DEFAULT_PBKDF2_SALT_SIZE_BYTES = 32;
  private final int DEFAULT_AES_KEY_LENGTH_BITS = 256; /* @see https://www.keylength.com/ */
  private final String DEFAULT_CIPHER = "AES";
  private final String DEFAULT_CIPHERSCHEME = "AES/GCM/PKCS5Padding";
  private final String DEFAULT_PBKDF2_SCHEME = "PBKDF2WithHmacSHA256";

  private int GCM_AUTHENTICATION_TAG_SIZE_BITS = DEFAULT_GCM_AUTHENTICATION_TAG_SIZE_BITS;
  private int GCM_IV_NONCE_SIZE_BYTES = DEFAULT_GCM_IV_NONCE_SIZE_BYTES;
  private int PBKDF2_ITERATIONS = DEFAULT_PBKDF2_ITERATIONS;
  private int PBKDF2_SALT_SIZE_BYTES = DEFAULT_PBKDF2_SALT_SIZE_BYTES;
  private int AES_KEY_LENGTH_BITS = DEFAULT_AES_KEY_LENGTH_BITS;
  private String CIPHER = DEFAULT_CIPHER;
  private String CIPHERSCHEME = DEFAULT_CIPHERSCHEME;
  private String PBKDF2_SCHEME = DEFAULT_PBKDF2_SCHEME;

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
   * Initializes this EncryptedString object with the provided parameters
   * @param CIPHER
   * @param CIPHERSCHEME
   * @param GCM_AUTHENTICATION_TAG_SIZE_BITS
   * @param GCM_IV_NONCE_SIZE_BYTES
   * @param PBKDF2_ITERATIONS
   * @param PBKDF2_SALT_SIZE_BYTES
   * @param AES_KEY_LENGTH_BITS
   * @param PBKDF2_SCHEME
   */
  private EncryptedString(String cipherText, byte[] nonce, byte[] salt, String CIPHER, String CIPHERSCHEME, int GCM_AUTHENTICATION_TAG_SIZE_BITS, int GCM_IV_NONCE_SIZE_BYTES, int PBKDF2_ITERATIONS, int PBKDF2_SALT_SIZE_BYTES, int AES_KEY_LENGTH_BITS, String PBKDF2_SCHEME) {
    this.cipherText = cipherText;
    this.nonce = nonce;
    this.salt = salt;

    this.CIPHER = CIPHER;
    this.CIPHERSCHEME = CIPHERSCHEME;
    this.GCM_AUTHENTICATION_TAG_SIZE_BITS = GCM_AUTHENTICATION_TAG_SIZE_BITS;
    this.GCM_IV_NONCE_SIZE_BYTES = GCM_IV_NONCE_SIZE_BYTES;
    this.PBKDF2_ITERATIONS = PBKDF2_ITERATIONS;
    this.PBKDF2_SALT_SIZE_BYTES = PBKDF2_SALT_SIZE_BYTES;
    this.AES_KEY_LENGTH_BITS = AES_KEY_LENGTH_BITS;
    this.PBKDF2_SCHEME = PBKDF2_SCHEME;
  }

  /**
   * Creates a new empty EncryptedString object
   */
  public EncryptedString() {
    // uses default parameters, see initialization at the beginning.
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
   * Generates a randomly filled byte array
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
    SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_SCHEME);
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

    return new EncryptedString(new String(Base64.getEncoder().encode(byteCipher)), nonce, salt, CIPHER, CIPHERSCHEME, GCM_AUTHENTICATION_TAG_SIZE_BITS, GCM_IV_NONCE_SIZE_BYTES, PBKDF2_ITERATIONS, PBKDF2_SALT_SIZE_BYTES, AES_KEY_LENGTH_BITS, PBKDF2_SCHEME);
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
    SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_SCHEME);
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


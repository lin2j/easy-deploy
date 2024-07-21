package tech.lin2j.idea.plugin.uitl;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Copied parts of {@link com.intellij.ide.passwordSafe.impl.providers.EncryptionUtil}
 * to prevent certain APIs from becoming unavailable due to IDEA version upgrades.
 *
 * @author linjinjia
 * @date 2024/7/21 13:54
 * @see com.intellij.ide.passwordSafe.impl.providers.EncryptionUtil
 */
public class EncryptionUtil {

    /**
     * The hash algorithm used for keys
     */
    private static final String SECRET_KEY_ALGORITHM = "AES";
    /**
     * The hash algorithm used for encrypting data
     */
    private static final String ENCRYPT_DATA_ALGORITHM = "AES/CBC/PKCS5Padding";
    /**
     * The secret key size (available for international encryption)
     */
    private static final int SECRET_KEY_SIZE = 128;
    /**
     * The secret key size (available for international encryption)
     */
    public static final int SECRET_KEY_SIZE_BYTES = SECRET_KEY_SIZE / 8;

    /**
     * 128 bits salt for AES-CBC with for data values (stable non-secret value)
     */
    private static final IvParameterSpec CBC_SALT_DATA = new IvParameterSpec(new byte[]{119, 111, -93, 2, -43, -12, 117, 82, 12, 40, 69, -34, 78, 86, -97, 95});

    /**
     * File read/write buffer
     */
    private static final int BUFFER_SIZE = 8192;


    /**
     * The private constructor
     */
    private EncryptionUtil() {
        // do nothing
    }

    /**
     * Generates a Cipher object for AES encryption/decryption using the given password and mode.
     *
     * <p>This method uses the given password to generate a SecretKeySpec, which is then used
     * to initialize the Cipher object in the specified mode (either encryption or decryption).
     * The password is hashed using the SHA-1 algorithm, and the first 128 bits of the hash
     * are used as the AES key. The CBC mode with a specified initialization vector (IV) is used
     * to ensure secure encryption.</p>
     *
     * @param password the password to generate the AES key from
     * @param mode     the cipher mode (Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE)
     * @return the initialized Cipher object
     * @throws IllegalStateException if any error occurs during the key generation or cipher initialization
     */
    public static Cipher getCipher(String password, int mode) {
        try {
            byte[] secretKey = password.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            secretKey = sha.digest(secretKey);
            secretKey = Arrays.copyOf(secretKey, SECRET_KEY_SIZE_BYTES);
            Key key = new SecretKeySpec(secretKey, SECRET_KEY_ALGORITHM);

            Cipher cipher = Cipher.getInstance(ENCRYPT_DATA_ALGORITHM);
            cipher.init(mode, key, CBC_SALT_DATA);
            return cipher;
        } catch (Exception e) {
            throw new IllegalStateException(ENCRYPT_DATA_ALGORITHM + " is not available", e);
        }
    }


    /**
     * Encrypts the given content and writes it into a specified file using AES encryption.
     *
     * <p>This method takes a content string, encrypts it using AES with the given password,
     * and writes the encrypted content into the specified file. The encryption is done in
     * CBC mode with PKCS5 padding.</p>
     *
     * @param content  the content to be encrypted
     * @param filename the name of the file to write the encrypted content into
     * @param password the password to generate the AES key for encryption
     * @throws IOException if an I/O error occurs
     */
    public static void encryptContentIntoFile(String content, String filename, String password) throws IOException {
        Cipher cipher = getCipher(password, Cipher.ENCRYPT_MODE);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                FileOutputStream fos = new FileOutputStream(filename);
                CipherOutputStream cos = new CipherOutputStream(fos, cipher)
        ) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Decrypts the content of a specified file using AES decryption and returns it as a string.
     *
     * <p>This method reads the encrypted content from the specified file, decrypts it using AES with the given password,
     * and returns the decrypted content as a string. The decryption is done in CBC mode with PKCS5 padding.</p>
     *
     * @param filename the name of the file containing the encrypted content
     * @param password the password to generate the AES key for decryption
     * @return the decrypted content as a string
     * @throws IOException if an I/O error occurs
     */
    public static String decryptFileContent(String filename, String password) throws IOException {
        Cipher cipher = getCipher(password, Cipher.DECRYPT_MODE);
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                FileInputStream fis = new FileInputStream(filename);
                CipherInputStream cis = new CipherInputStream(fis, cipher)
        ) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = cis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toString();
        }
    }
}
import java.io.*;

public class TripleDES {
    private DES des1;
    private DES des2;
    private DES des3;

    /* Constructor */
    public TripleDES(byte[] keyPart1, byte[] keyPart2, byte[] keyPart3) {
        if (keyPart1.length != 8 || keyPart2.length != 8 || keyPart3.length != 8) {
            throw new IllegalArgumentException("Each key part must be 8 bytes long.");
        }
        des1 = new DES(keyPart1);
        des2 = new DES(keyPart2);
        des3 = new DES(keyPart3);
    }

    /* Encrypt plaintext block */
    public byte[] encryptBytes(byte[] plaintextBytes) {
        if (plaintextBytes.length < 8) {
            throw new IllegalArgumentException("Plaintext must be at least 8 bytes long.");
        }

        byte[] intermediate1 = new byte[8];
        byte[] intermediate2 = new byte[8];
        byte[] ciphertext = new byte[8];

        // First encryption
        des1.encrypt(plaintextBytes, 0, intermediate1, 0);
        // Decryption with second key
        des2.decrypt(intermediate1, 0, intermediate2, 0);
        // Second encryption with third key
        des3.encrypt(intermediate2, 0, ciphertext, 0);

        return ciphertext;
    }

    /* Decrypt ciphertext block */
    public byte[] decryptBytes(byte[] ciphertextBytes) {
        if (ciphertextBytes.length < 8) {
            throw new IllegalArgumentException("Ciphertext must be at least 8 bytes long.");
        }

        byte[] intermediate1 = new byte[8];
        byte[] intermediate2 = new byte[8];
        byte[] plaintext = new byte[8];

        // First decryption with third key
        des3.decrypt(ciphertextBytes, 0, intermediate1, 0);
        // Encryption with second key
        des2.encrypt(intermediate1, 0, intermediate2, 0);
        // Second decryption with first key
        des1.decrypt(intermediate2, 0, plaintext, 0);

        return plaintext;
    }

    private String byteArraytoHexString(byte[] byteArray) {
        StringBuilder ret = new StringBuilder();
        for (byte b : byteArray) {
            ret.append(String.format("%02x", b)).append(" ");
        }
        return ret.toString();
    }

    public static void main(String[] args) {
        /* Test code */
        TripleDES cipher = new TripleDES("qwertzui".getBytes(), "asdfghjk".getBytes(), "yxcvbnm,".getBytes());

        byte[] plain = "12345678".getBytes();
        byte[] chiffre = cipher.encryptBytes(plain);
        System.out.println("Encrypted: " + cipher.byteArraytoHexString(plain) + " to: " + cipher.byteArraytoHexString(chiffre));

        byte[] plainNew = cipher.decryptBytes(chiffre);
        System.out.println("Decrypted: " + cipher.byteArraytoHexString(plainNew));

        if (java.util.Arrays.equals(plain, plainNew)) {
            System.out.println(" ---> Erfolg!");
        } else {
            System.out.println(" ---> Hat leider noch nicht funktioniert ...!");
        }
    }
}
